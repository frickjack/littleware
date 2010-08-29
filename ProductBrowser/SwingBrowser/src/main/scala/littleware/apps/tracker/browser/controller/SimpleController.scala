/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.browser.controller

import com.google.inject.Inject
import littleware.apps.tracker.{Member,Product,Version, ProductManager}
import littleware.apps.tracker.browser.model
import littleware.asset.AssetManager
import littleware.asset.AssetPathFactory
import littleware.asset.AssetSearchManager
import littleware.base.feedback.Feedback

class SimpleController @Inject() ( assetMgr:AssetManager, 
                                  search:AssetSearchManager,
                                  productMgr:ProductManager
) extends Controller {
  override def createProduct( productData:model.ProductData ):Product = {
    val parent = search.getAssetAtPath(productData.parentPath).get
    val product:Product = Product.ProductType.create.parent( parent
    ).name( productData.name
    ).comment( productData.comment
    ).build.narrow[Product]
    assetMgr.saveAsset( product, "Setup product" )
  }

  override def createVersion( versionData:model.VersionData ):Version = {
    val version:Version = Version.VersionType.create.product( versionData.product
        ).name( versionData.name
        ).comment( versionData.comment
        ).build.narrow( classOf[Version])
    assetMgr.saveAsset( version, "Setup version" )
  }

  def createMember( memberData:model.MemberData ):Member = {
    val member:Member = Member.MemberType.create.version(memberData.version
    ).name( memberData.name
    ).comment( memberData.comment
    ).data( memberData.data
    ).build.narrow( classOf[Member] )
    assetMgr.saveAsset( member, "Setup member" )
  }

  override def checkin( checkinData:model.MemberCheckinData, feedback:Feedback ):Member =
    productMgr.checkin( checkinData.version.getId,
                       checkinData.name,
                       checkinData.dataDir,
                       checkinData.comment,
                       feedback
    )

  override def checkout( checkoutData:model.MemberCheckoutData, feedback:Feedback ):Unit =
    productMgr.checkout(checkoutData.member.getId, checkoutData.destinationDir, feedback )
}
