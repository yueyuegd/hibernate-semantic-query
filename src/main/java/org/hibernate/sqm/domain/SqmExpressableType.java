/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.domain;

import org.hibernate.sqm.domain.type.SqmDomainTypeBasic;

/**
 * Used to polymorphically handle anything that can be the "type" of an expression.
 * Generally speaking this is either a {@link SqmDomainTypeBasic}
 * {@link org.hibernate.sqm.domain.SqmNavigable}
 *
 * @author Steve Ebersole
 */
public interface SqmExpressableType<T> extends SqmDomainTypeExporter<T> {
}
