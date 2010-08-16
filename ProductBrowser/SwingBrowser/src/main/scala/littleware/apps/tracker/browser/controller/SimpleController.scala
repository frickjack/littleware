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
import littleware.apps.tracker.Product
import littleware.apps.tracker.browser.model.ProductData
import littleware.asset.AssetManager
import littleware.asset.AssetPathFactory
import littleware.asset.AssetSearchManager

class SimpleController @Inject() ( assetMgr:AssetManager, 
                                  search:AssetSearchManager,
                                  pathFactory:AssetPathFactory
) extends Controller {
  override def createProduct( productData:ProductData ):Product = {
    val parentPath = pathFactory.createPath( productData.parentPath )
    val parent = search.getAssetAtPath(parentPath).get
    val product:Product = Product.ProductType.create.parent( parent
    ).name( productData.name
    ).comment( productData.comment
    ).build.narrow[Product]
    assetMgr.saveAsset( product, "Setup product" )
  }
}
