/*
 * @(#)AbstractActivityAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action;

import javafx.event.ActionEvent;
import org.jhotdraw8.application.Activity;
import org.jhotdraw8.application.Application;

/**
 * This abstract class can be extended to implement an {@code Action} that acts
 * on the active {@link Activity}, or on a specific {@code Activity}.
 * <p>
 * If the active view or the specified view is disabled, the
 * AbstractViewControllerAction is disabled as well.
 *
 * @param <A> the activity type
 */
public abstract class AbstractActivityAction<A extends Activity> extends AbstractApplicationAction {

    private final A activity;

    /**
     * Creates a new instance which acts on the specified activity of the
     * application.
     *
     * @param activity The activity. If activity is null then the action acts on
     *                 the active activity of the application. Otherwise, it will act on the
     *                 specified activity.
     */
    public AbstractActivityAction(A activity) {
        super(activity.getApplication());
        this.activity = activity;
    }

    public A getActivity() {
        return activity;
    }

    @Override
    protected final void onActionPerformed(ActionEvent event, Application app) {
        onActionPerformed(event, getActivity());
    }

    /**
     * This method is invoked when the action is not disabled and the event is
     * not consumed.
     *
     * @param event    the action event
     * @param activity the activity
     */
    protected abstract void onActionPerformed(ActionEvent event, A activity);


}
