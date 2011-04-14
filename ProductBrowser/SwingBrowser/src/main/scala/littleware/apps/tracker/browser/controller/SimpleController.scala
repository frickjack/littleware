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
import com.google.inject.Provider
import java.security.GeneralSecurityException
import java.util.UUID
import java.util.logging.Level
import littleware.apps.client.AssetModelLibrary
import littleware.apps.tracker.browser.model
import littleware.asset.Asset
import littleware.asset.AssetManager
import littleware.asset.AssetPathFactory
import littleware.asset.AssetSearchManager
import littleware.asset.AssetType
import littleware.base.BaseException
import littleware.base.feedback.Feedback
import scala.collection.JavaConversions._
import littleware.scala.LazyLogger
import scala.Option._


class SimpleController @Inject() ( assetMgr:AssetManager, 
                                  search:AssetSearchManager,
                                  productMgr:ProductManager,
                                  assetLib:AssetModelLibrary,
                                  pathFactory:AssetPathFactory,
                                  hoodProvider:Provider[model.Neighborhood.Builder],
                                  neighborProvider:Provider[model.NeighborInfo.Builder]
) extends Controller {
  private val log = LazyLogger( getClass )

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

  @throws(classOf[BaseException])
  @throws(classOf[GeneralSecurityException])
  override def loadNeighborhood( assetId:UUID ):model.Neighborhood = {
    /*
     * Build the NeighborInfo for the asset with the given id
     */
    @throws(classOf[BaseException])
    @throws(classOf[GeneralSecurityException])
    def loadInfo( infoId:UUID, relativePath:String ):Option[model.NeighborInfo] =
      try {
        Option(search.getAsset(infoId).getOr(null)).map( (asset) =>
          neighborProvider.get.model( assetLib.syncAsset( asset )
          ).absPath( pathFactory.toRootedPath( infoId )
          ).relativePath( relativePath
          ).build
        )
      } catch {
        case ex => {
            log.log( Level.WARNING, "Failed to load neighbor info for " + infoId, ex )
            None
          }
      }

    /*
     * Extract a (name -> id) link map for the given asset
     */
    def buildLinkMap( asset:Asset ):Map[String,UUID] = {
      Map.empty[String,UUID] ++ asset.getLinkMap ++
      Map(
        ("owner" -> asset.getOwnerId),
        ("acl" -> asset.getAclId),
        ("creator" -> asset.getCreatorId),
        ("updater" -> asset.getLastUpdaterId),
        ("from" -> asset.getFromId),
        ("home" -> asset.getHomeId),
        ("to" -> asset.getToId)
      ).filter( { _._2 != null } )
    }

    val builder = hoodProvider.get
    val asset:Asset = search.getAsset(assetId).get
    val parent:Option[Asset] = Option(asset.getFromId).map( search.getAsset(_).get )

    builder.asset( loadInfo(assetId, ".").get
    ).children(
      search.getAssetIdsFrom(assetId).entrySet.flatMap( (entry) => loadInfo( entry.getValue, "./" + entry.getKey )).toSeq
    ).uncles(
      parent.toSeq.flatMap( (parentAsset) => { Option( parentAsset.getFromId ) }
      ).flatMap( (grannyId) => search.getAssetIdsFrom( grannyId ).entrySet
      ).flatMap( (uncleEntry) => loadInfo(uncleEntry.getValue, "../../" + uncleEntry.getKey )
      )
    ).siblings(
      (
        if ( asset.getAssetType == AssetType.HOME) {
          // HOME type assets are siblings of each other in the browser
          search.getHomeAssetIds.entrySet.toSeq
        } else {
          parent.toSeq.flatMap( (parentAsset) => search.getAssetIdsFrom( parentAsset.getId ).entrySet )
        }
      ).filter( (siblingEntry) => { siblingEntry.getValue != assetId }
      ).flatMap( (siblingEntry) => loadInfo(siblingEntry.getValue, "../" + siblingEntry.getKey )
      )
    ).neighbors(      
        buildLinkMap(asset).flatMap(
          _ match { case (property,propertyId) => loadInfo(propertyId, "./@" + property) }
      ).toSeq
    ).build
  }
}
