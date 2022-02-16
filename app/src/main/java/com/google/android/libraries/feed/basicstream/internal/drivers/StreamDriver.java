// Copyright 2018 The Feed Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.android.libraries.feed.basicstream.internal.drivers;

import static com.google.android.libraries.feed.common.Validators.checkNotNull;

import android.content.Context;
import androidx.annotation.VisibleForTesting;
import com.google.android.libraries.feed.api.actionmanager.ActionManager;
import com.google.android.libraries.feed.api.actionparser.ActionParserFactory;
import com.google.android.libraries.feed.api.common.ThreadUtils;
import com.google.android.libraries.feed.api.knowncontent.KnownContentApi;
import com.google.android.libraries.feed.api.modelprovider.FeatureChange;
import com.google.android.libraries.feed.api.modelprovider.FeatureChangeObserver;
import com.google.android.libraries.feed.api.modelprovider.ModelChild;
import com.google.android.libraries.feed.api.modelprovider.ModelChild.Type;
import com.google.android.libraries.feed.api.modelprovider.ModelCursor;
import com.google.android.libraries.feed.api.modelprovider.ModelFeature;
import com.google.android.libraries.feed.api.modelprovider.ModelProvider;
import com.google.android.libraries.feed.api.modelprovider.ModelProvider.State;
import com.google.android.libraries.feed.api.stream.ContentChangedListener;
import com.google.android.libraries.feed.basicstream.internal.drivers.ContinuationDriver.CursorChangedListener;
import com.google.android.libraries.feed.basicstream.internal.scroll.ScrollRestorer;
import com.google.android.libraries.feed.common.logging.Logger;
import com.google.android.libraries.feed.common.time.Clock;
import com.google.android.libraries.feed.host.action.ActionApi;
import com.google.android.libraries.feed.host.config.Configuration;
import com.google.android.libraries.feed.host.logging.BasicLoggingApi;
import com.google.android.libraries.feed.host.stream.SnackbarApi;
import com.google.android.libraries.feed.sharedstream.contextmenumanager.ContextMenuManager;
import com.google.android.libraries.feed.sharedstream.offlinemonitor.StreamOfflineMonitor;
import com.google.android.libraries.feed.sharedstream.removetrackingfactory.StreamRemoveTrackingFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Generates a list of {@link LeafFeatureDriver} instances for an entire stream. */
public class StreamDriver implements CursorChangedListener, FeatureChangeObserver {

  private static final String TAG = "StreamDriver";
  private final ActionApi actionApi;
  private final ActionManager actionManager;
  private final ActionParserFactory actionParserFactory;
  private final ThreadUtils threadUtils;
  private final ModelProvider modelProvider;
  private final Map<ModelChild, FeatureDriver> modelChildFeatureDriverMap;
  private final List<FeatureDriver> featureDrivers;
  private final Clock clock;
  private final Configuration configuration;
  private final Context context;
  private final SnackbarApi snackbarApi;
  private final ContentChangedListener contentChangedListener;
  private final ScrollRestorer scrollRestorer;
  private final BasicLoggingApi basicLoggingApi;
  private final StreamOfflineMonitor streamOfflineMonitor;
  private final ContextMenuManager contextMenuManager;
  private final boolean isInitialLoad;

  private boolean restoring;
  private boolean rootFeatureConsumed;
  private boolean modelFeatureChangeObserverRegistered;

  /*@Nullable*/ private StreamContentListener contentListener;

  public StreamDriver(
      ActionApi actionApi,
      ActionManager actionManager,
      ActionParserFactory actionParserFactory,
      ModelProvider modelProvider,
      ThreadUtils threadUtils,
      Clock clock,
      Configuration configuration,
      Context context,
      SnackbarApi snackbarApi,
      ContentChangedListener contentChangedListener,
      ScrollRestorer scrollRestorer,
      BasicLoggingApi basicLoggingApi,
      StreamOfflineMonitor streamOfflineMonitor,
      KnownContentApi knownContentApi,
      ContextMenuManager contextMenuManager,
      boolean restoring,
      boolean isInitialLoad) {
    this.actionApi = actionApi;
    this.actionManager = actionManager;
    this.actionParserFactory = actionParserFactory;
    this.threadUtils = threadUtils;
    this.modelProvider = modelProvider;
    this.clock = clock;
    this.context = context;
    this.snackbarApi = snackbarApi;
    this.contextMenuManager = contextMenuManager;
    this.modelChildFeatureDriverMap = new HashMap<>();
    this.featureDrivers = new ArrayList<>();
    this.configuration = configuration;
    this.contentChangedListener = contentChangedListener;
    this.scrollRestorer = scrollRestorer;
    this.basicLoggingApi = basicLoggingApi;
    this.streamOfflineMonitor = streamOfflineMonitor;
    this.restoring = restoring;
    this.isInitialLoad = isInitialLoad;

    modelProvider.enableRemoveTracking(
        new StreamRemoveTrackingFactory(modelProvider, knownContentApi));
  }

  /**
   * Returns a the list of {@link LeafFeatureDriver} instances for the children generated from the
   * given {@link ModelFeature}.
   */
  public List<LeafFeatureDriver> getLeafFeatureDrivers() {
    if (modelProvider.getCurrentState() == State.READY && !rootFeatureConsumed) {
      rootFeatureConsumed = true;
      ModelFeature rootFeature = modelProvider.getRootFeature();
      if (rootFeature != null) {
        createAndInsertChildren(rootFeature, modelProvider);
        rootFeature.registerObserver(this);
        modelFeatureChangeObserverRegistered = true;
      } else {
        Logger.w(TAG, "found null root feature loading Leaf Feature Drivers");
      }
    }

    if (!isInitialLoad) {
      addNoContentCardOrZeroStateIfNecessary();
    }

    return buildLeafFeatureDrivers(featureDrivers);
  }

  public void maybeRestoreScroll() {
    if (!restoring) {
      return;
    }

    // TODO: Handle continuation driver. This needs to handle synthetic tokens.
    if (isLastDriverContinuationDriver()) {
      ContinuationDriver continuationDriver =
          (ContinuationDriver) featureDrivers.get(featureDrivers.size() - 1);
      if (continuationDriver.isSynthetic()) {
        return;
      }
    }

    restoring = false;

    scrollRestorer.maybeRestoreScroll();
  }

  private List<FeatureDriver> createAndInsertChildren(
      ModelFeature streamFeature, ModelProvider modelProvider) {
    return createAndInsertChildren(streamFeature.getCursor(), modelProvider);
  }

  private List<FeatureDriver> createAndInsertChildren(
      ModelCursor streamCursor, ModelProvider modelProvider) {
    return createAndInsertChildrenAtIndex(streamCursor, modelProvider, 0);
  }

  private List<FeatureDriver> createAndInsertChildrenAtIndex(
      ModelCursor streamCursor, ModelProvider modelProvider, int insertionIndex) {

    Iterable<ModelChild> cursorIterable =
        () ->
            new Iterator<ModelChild>() {
              /*@Nullable*/ private ModelChild next;

              @Override
              public boolean hasNext() {
                next = streamCursor.getNextItem();
                return next != null;
              }

              @Override
              public ModelChild next() {
                return checkNotNull(next);
              }
            };
    return createAndInsertChildrenAtIndex(cursorIterable, modelProvider, insertionIndex);
  }

  private List<FeatureDriver> createAndInsertChildrenAtIndex(
      Iterable<ModelChild> modelChildren, ModelProvider modelProvider, int insertionIndex) {
    List<FeatureDriver> newChildren = new ArrayList<>();

    for (ModelChild child : modelChildren) {
      FeatureDriver featureDriverChild = createChild(child, modelProvider, insertionIndex);
      if (featureDriverChild != null) {
        newChildren.add(featureDriverChild);
        featureDrivers.add(insertionIndex, featureDriverChild);
        modelChildFeatureDriverMap.put(child, featureDriverChild);
        insertionIndex++;
      }
    }

    return newChildren;
  }

  /*@Nullable*/
  private FeatureDriver createChild(ModelChild child, ModelProvider modelProvider, int position) {
    switch (child.getType()) {
      case Type.FEATURE:
        return createFeatureChild(child.getModelFeature(), position);
      case Type.TOKEN:
        ContinuationDriver continuationDriver =
            createContinuationDriver(
                basicLoggingApi,
                clock,
                configuration,
                context,
                child,
                modelProvider,
                position,
                snackbarApi,
                restoring);

        // TODO: Look into moving initialize() into a more generic location. We don't
        // really want work to be done in the constructor so we call an initialize() method to
        // kick off any expensive work the driver may need to do.
        continuationDriver.initialize();
        return continuationDriver;
      case Type.UNBOUND:
        Logger.e(TAG, "Found unbound child %s, ignoring it", child.getContentId());
        return null;
      default:
        Logger.wtf(TAG, "Received illegal child: %s from cursor.", child.getType());
        return null;
    }
  }

  private /*@Nullable*/ FeatureDriver createFeatureChild(ModelFeature modelFeature, int position) {
    if (modelFeature.getStreamFeature().hasCard()) {
      return createCardDriver(modelFeature, position);
    } else if (modelFeature.getStreamFeature().hasCluster()) {
      return createClusterDriver(modelFeature, position);
    }

    Logger.w(
        TAG,
        "Invalid StreamFeature Type, must be Card or Cluster but was %s",
        modelFeature.getStreamFeature().getFeaturePayloadCase());
    return null;
  }

  private List<LeafFeatureDriver> buildLeafFeatureDrivers(List<FeatureDriver> featureDrivers) {
    List<LeafFeatureDriver> leafFeatureDrivers = new ArrayList<>();
    List<FeatureDriver> removes = new ArrayList<>();
    for (FeatureDriver featureDriver : featureDrivers) {
      LeafFeatureDriver childContentModel = featureDriver.getLeafFeatureDriver();
      if (childContentModel != null) {
        leafFeatureDrivers.add(childContentModel);
      } else {
        removes.add(featureDriver);
      }
    }
    for (FeatureDriver driver : removes) {
      this.featureDrivers.remove(driver);
      driver.onDestroy();
    }

    streamOfflineMonitor.requestOfflineStatusForNewContent();

    return leafFeatureDrivers;
  }

  @Override
  public void onChange(FeatureChange change) {
    Logger.v(TAG, "Received change.");

    List<ModelChild> removedChildren = change.getChildChanges().getRemovedChildren();

    for (ModelChild removedChild : removedChildren) {
      if (!(removedChild.getType() == Type.FEATURE || removedChild.getType() == Type.TOKEN)) {
        Logger.e(
            TAG, "Attempting to remove non-removable child of type: %s", removedChild.getType());
        continue;
      }

      removeDriver(removedChild);
    }

    List<ModelChild> appendedChildren = change.getChildChanges().getAppendedChildren();

    if (!appendedChildren.isEmpty()) {
      int insertionIndex = featureDrivers.size();

      notifyContentsAdded(
          insertionIndex,
          buildLeafFeatureDrivers(
              createAndInsertChildrenAtIndex(appendedChildren, modelProvider, insertionIndex)));
    }

    addNoContentCardOrZeroStateIfNecessary();
  }

  @Override
  public void onNewChildren(ModelChild modelChild, List<ModelChild> modelChildren) {
    int continuationIndex = removeDriver(modelChild);
    if (continuationIndex < 0) {
      Logger.wtf(TAG, "Received an onNewChildren for an unknown child.");
      return;
    }
    List<FeatureDriver> newChildren =
        createAndInsertChildrenAtIndex(modelChildren, modelProvider, continuationIndex);

    notifyContentsAdded(continuationIndex, buildLeafFeatureDrivers(newChildren));
    if (shouldRemoveNoContentCardOrZeroState()) {
      featureDrivers.get(0).onDestroy();
      featureDrivers.remove(0);
      notifyContentRemoved(0);
    }
    // Swap no content card with zero state if there are no more drivers.
    if (newChildren.isEmpty()
        && featureDrivers.size() == 1
        && featureDrivers.get(0) instanceof NoContentDriver) {
      showZeroState(false);
    }

    maybeRestoreScroll();
  }

  public void onDestroy() {
    for (FeatureDriver featureDriver : featureDrivers) {
      featureDriver.onDestroy();
    }
    ModelFeature modelFeature = modelProvider.getRootFeature();
    if (modelFeature != null && modelFeatureChangeObserverRegistered) {
      modelFeature.unregisterObserver(this);
      modelFeatureChangeObserverRegistered = false;
    }
    featureDrivers.clear();
    modelChildFeatureDriverMap.clear();
  }

  public boolean hasContent() {
    if (featureDrivers.isEmpty()) {
      return false;
    }
    return !(featureDrivers.get(0) instanceof NoContentDriver)
        && !(featureDrivers.get(0) instanceof ZeroStateDriver);
  }

  private void addNoContentCardOrZeroStateIfNecessary() {
    LeafFeatureDriver leafFeatureDriver = null;
    if (!restoring && featureDrivers.isEmpty()) {
      leafFeatureDriver = createZeroStateDriver(/* showSpinner= */ false);
    } else if (featureDrivers.size() == 1 && isLastDriverContinuationDriver()) {
      leafFeatureDriver = createNoContentDriver();
    }

    if (leafFeatureDriver != null) {
      featureDrivers.add(0, leafFeatureDriver);
      notifyContentsAdded(0, Collections.singletonList(leafFeatureDriver));
    }
  }

  private boolean shouldRemoveNoContentCardOrZeroState() {
    if (featureDrivers.isEmpty()) {
      return false;
    }

    if (!(featureDrivers.get(0) instanceof NoContentDriver)
        && !(featureDrivers.get(0) instanceof ZeroStateDriver)) {
      return false;
    }

    return featureDrivers.size() > 2
        || (featureDrivers.size() == 2 && !isLastDriverContinuationDriver());
  }

  private boolean isLastDriverContinuationDriver() {
    return !featureDrivers.isEmpty()
        && featureDrivers.get(featureDrivers.size() - 1) instanceof ContinuationDriver;
  }

  /**
   * Removes the {@link FeatureDriver} represented by the {@link ModelChild} from all collections
   * containing it and updates any listening instances of {@link StreamContentListener} of the
   * removal.
   *
   * <p>Returns the index at which the {@link FeatureDriver} was removed, or -1 if it was not found.
   */
  private int removeDriver(ModelChild modelChild) {
    FeatureDriver featureDriver = modelChildFeatureDriverMap.get(modelChild);
    if (featureDriver == null) {
      Logger.w(TAG, "Attempting to remove feature from ModelChild not in map.");
      return -1;
    }

    for (int i = 0; i < featureDrivers.size(); i++) {
      if (featureDrivers.get(i) == featureDriver) {
        featureDrivers.remove(i);
        featureDriver.onDestroy();
        modelChildFeatureDriverMap.remove(modelChild);
        notifyContentRemoved(i);
        return i;
      }
    }

    Logger.wtf(TAG, "Attempting to remove feature contained on map but not on list of children.");
    return -1;
  }

  private void notifyContentsAdded(int index, List<LeafFeatureDriver> leafFeatureDrivers) {
    if (contentListener != null) {
      contentListener.notifyContentsAdded(index, leafFeatureDrivers);
    }
  }

  private void notifyContentRemoved(int index) {
    if (contentListener != null) {
      contentListener.notifyContentRemoved(index);
    }
  }

  private void notifyContentsCleared() {
    if (contentListener != null) {
      contentListener.notifyContentsCleared();
    }
  }

  public void showZeroState(boolean showSpinner) {
    ZeroStateDriver zeroStateDriver = createZeroStateDriver(showSpinner);
    // TODO: Make sure to not notify listeners when driver is destroyed.
    for (FeatureDriver featureDriver : featureDrivers) {
      featureDriver.onDestroy();
    }
    featureDrivers.clear();
    notifyContentsCleared();
    featureDrivers.add(zeroStateDriver);
    notifyContentsAdded(0, Collections.singletonList(zeroStateDriver));
  }

  @VisibleForTesting
  FeatureDriver createClusterDriver(ModelFeature modelFeature, int position) {
    return new ClusterDriver(
        actionApi,
        actionManager,
        actionParserFactory,
        basicLoggingApi,
        modelFeature,
        modelProvider,
        position,
        streamOfflineMonitor,
        contentChangedListener,
        contextMenuManager);
  }

  @VisibleForTesting
  FeatureDriver createCardDriver(ModelFeature modelFeature, int position) {
    return new CardDriver(
        actionApi,
        actionManager,
        actionParserFactory,
        basicLoggingApi,
        modelFeature,
        modelProvider,
        position,
        streamOfflineMonitor,
        contentChangedListener,
        contextMenuManager);
  }

  @VisibleForTesting
  ContinuationDriver createContinuationDriver(
      BasicLoggingApi basicLoggingApi,
      Clock clock,
      Configuration configuration,
      Context context,
      ModelChild modelChild,
      ModelProvider modelProvider,
      int position,
      SnackbarApi snackbarApi,
      boolean restoring) {
    return new ContinuationDriver(
        basicLoggingApi,
        clock,
        configuration,
        context,
        this,
        modelChild,
        modelProvider,
        position,
        snackbarApi,
        threadUtils,
        // We always want to automatically consume synthetic tokens when we are restoring.
        /*forceAutoConsumeSyntheticTokens= */ restoring);
  }

  @VisibleForTesting
  NoContentDriver createNoContentDriver() {
    return new NoContentDriver();
  }

  @VisibleForTesting
  ZeroStateDriver createZeroStateDriver(boolean showSpinner) {
    return new ZeroStateDriver(
        basicLoggingApi, clock, modelProvider, contentChangedListener, showSpinner);
  }

  public void setStreamContentListener(/*@Nullable*/ StreamContentListener contentListener) {
    this.contentListener = contentListener;
  }

  /** Allows listening for changes in the contents held by a {@link StreamDriver} */
  public interface StreamContentListener {

    /** Called when the given content has been added at the given index of stream content. */
    void notifyContentsAdded(int index, List<LeafFeatureDriver> newFeatureDrivers);

    /** Called when the content at the given index of stream content has been removed. */
    void notifyContentRemoved(int index);

    /** Called when the content in the stream has been cleared. */
    void notifyContentsCleared();
  }
}
