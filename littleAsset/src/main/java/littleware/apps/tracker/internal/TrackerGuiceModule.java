/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.internal;

import com.google.inject.Binder;
import com.google.inject.Module;
import littleware.apps.tracker.Comment;
import littleware.apps.tracker.Member;
import littleware.apps.tracker.MemberAlias;
import littleware.apps.tracker.Product;
import littleware.apps.tracker.ProductAlias;
import littleware.apps.tracker.Queue;
import littleware.apps.tracker.Task;
import littleware.apps.tracker.Version;
import littleware.apps.tracker.VersionAlias;


/** 
 * AssetType specializer and bucket for littleware.apps.tracker
 * based AssetTypes.  Implements guice Module interface that
 * different littleware modules can delegate to to bind
 * Providers for Task.TaskBuilder, Comment.CommentBuilder, ...
 */
public class TrackerGuiceModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind( Task.TaskBuilder.class ).to( SimpleTaskBuilder.class );
        binder.bind( Queue.QueueBuilder.class ).to( SimpleQueueBuilder.class );
        binder.bind( Comment.CommentBuilder.class ).to( SimpleCommentBuilder.class );
        binder.bind( Product.ProductBuilder.class ).to( SimpleProductBuilder.class );
        binder.bind( ProductAlias.PABuilder.class ).to( SimplePABuilder.class );
        binder.bind( Version.VersionBuilder.class ).to( SimpleVersionBuilder.class );
        binder.bind( VersionAlias.VABuilder.class ).to( SimpleVABuilder.class );
        binder.bind( Member.MemberBuilder.class ).to( SimpleMemberBuilder.class );
        binder.bind( MemberAlias.MABuilder.class ).to( SimpleMABuilder.class );
    }

}


