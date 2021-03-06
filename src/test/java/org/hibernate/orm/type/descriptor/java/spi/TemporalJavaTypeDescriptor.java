/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.java.spi;

import org.hibernate.orm.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public interface TemporalJavaTypeDescriptor<T> extends BasicJavaTypeDescriptor<T> {
	javax.persistence.TemporalType getPrecision();

	<X> TemporalJavaTypeDescriptor<X> resolveTypeForPrecision(
			javax.persistence.TemporalType precision,
			TypeConfiguration scope);
}
