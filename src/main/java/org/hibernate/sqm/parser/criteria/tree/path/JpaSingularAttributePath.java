/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.criteria.tree.path;

import org.hibernate.sqm.domain.SqmSingularAttribute;

/**
 * @author Steve Ebersole
 */
public interface JpaSingularAttributePath<T> extends JpaAttributePath<T> {
	@Override
	SqmSingularAttribute getNavigable();
}