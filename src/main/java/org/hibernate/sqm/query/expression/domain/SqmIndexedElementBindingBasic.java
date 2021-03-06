/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class SqmIndexedElementBindingBasic
		extends AbstractSqmIndexedElementBinding
		implements SqmRestrictedCollectionElementBindingBasic {
	public SqmIndexedElementBindingBasic(
			SqmPluralAttributeBinding pluralAttributeBinding,
			SqmExpression indexSelectionExpression) {
		super( pluralAttributeBinding, indexSelectionExpression );
	}
}
