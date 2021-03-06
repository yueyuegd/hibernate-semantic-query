/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.internal;

import java.util.Comparator;

import org.hibernate.orm.type.descriptor.java.spi.ManagedJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;
import org.hibernate.orm.type.spi.IdentifiableType;
import org.hibernate.orm.type.spi.ManagedType;
import org.hibernate.orm.type.spi.TypeConfiguration;
import org.hibernate.orm.type.spi.TypeConfigurationAware;

import org.jboss.logging.Logger;

/**
 * Base support for all ManagedType implementations.managed types, which is the JPA term for commonality between entity, embeddable and
 * "mapped superclass" types.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractManagedType extends AbstractTypeImpl implements ManagedType, TypeConfigurationAware {
	private static final Logger log = Logger.getLogger( AbstractManagedType.class );

	private final ManagedType superType;

	private TypeConfiguration typeConfiguration;

	public AbstractManagedType(ManagedType superType, ManagedJavaTypeDescriptor javaTypeDescriptor) {
		this(
				superType,
				javaTypeDescriptor,
				javaTypeDescriptor.getMutabilityPlan(),
				javaTypeDescriptor.getComparator()
		);
	}

	public AbstractManagedType(
			ManagedType superType,
			ManagedJavaTypeDescriptor javaTypeDescriptor,
			MutabilityPlan mutabilityPlan,
			Comparator comparator) {
		super( javaTypeDescriptor.getTypeName(), javaTypeDescriptor, mutabilityPlan, comparator );
		this.superType = superType;
	}

	@Override
	public TypeConfiguration getTypeConfiguration() {
		return typeConfiguration;
	}

	@Override
	public void setTypeConfiguration(TypeConfiguration typeConfiguration) {
		log.debugf( "Setting TypeConfiguration [%s] - was [%s]", typeConfiguration, this.typeConfiguration );
		this.typeConfiguration = typeConfiguration;
	}

	@Override
	public ManagedType getSuperType() {
		return superType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class getJavaType() {
		return getJavaTypeDescriptor().getJavaType();
	}
}
