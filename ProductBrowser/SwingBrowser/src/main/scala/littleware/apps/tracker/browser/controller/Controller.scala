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

import littleware.apps.tracker.{Product,Version, Member}
import java.security.GeneralSecurityException
import java.util.UUID
import littleware.apps.tracker.browser.model
import littleware.asset.AssetException
import littleware.base.BaseException
import littleware.base.feedback.Feedback

trait Controller {
  def createProduct( productData:model.ProductData ):Product
  def createVersion( versionData:model.VersionData ):Version
  def createMember( memberData:model.MemberData ):Member
  def checkin( checkinData:model.MemberCheckinData, feedback:Feedback ):Member
  def checkout( checkoutData:model.MemberCheckoutData, feedback:Feedback ):Unit
  @throws(classOf[BaseException])
  @throws(classOf[GeneralSecurityException])
  def loadNeighborhood( id:UUID ):model.AssetNeighborhood
}
