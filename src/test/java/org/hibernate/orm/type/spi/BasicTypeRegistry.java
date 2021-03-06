/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.spi;

import java.sql.Types;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EnumType;

import org.hibernate.HibernateException;
import org.hibernate.Incubating;
import org.hibernate.internal.util.compare.ComparableComparator;
import org.hibernate.orm.type.descriptor.java.internal.BigDecimalJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.BigIntegerJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.BlobJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.BooleanJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.ByteArrayJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.ByteJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.CalendarDateJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.CalendarTimeJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.CharacterArrayJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.CharacterJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.ClassJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.ClobJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.CurrencyJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.DoubleJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.DurationJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.FloatJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.InstantJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.IntegerJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.JdbcDateJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.JdbcTimeJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.JdbcTimestampJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.LocalDateJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.LocalDateTimeJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.LocalTimeJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.LocaleJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.LongJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.NClobJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.OffsetDateTimeJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.OffsetTimeJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.PrimitiveByteArrayJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.PrimitiveCharacterArrayJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.SerializableJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.ShortJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.StringJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.TimeZoneJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.UUIDJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.UrlJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.internal.ZonedDateTimeJavaDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.BasicJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.ImmutableMutabilityPlan;
import org.hibernate.orm.type.descriptor.java.spi.MutabilityPlan;
import org.hibernate.orm.type.descriptor.java.spi.TemporalJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.orm.type.descriptor.sql.spi.BigIntSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.BinarySqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.BlobSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.BooleanSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.CharSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.ClobSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.DateSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.DoubleSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.FloatSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.IntegerSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.LongNVarcharSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.LongVarbinarySqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.LongVarcharSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.NCharSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.NClobSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.NVarcharSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.NumericSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.SmallIntSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.TimeSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.TimestampSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.TinyIntSqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.VarbinarySqlDescriptor;
import org.hibernate.orm.type.descriptor.sql.spi.VarcharSqlDescriptor;
import org.hibernate.orm.type.internal.BasicTypeImpl;
import org.hibernate.orm.type.internal.TemporalTypeImpl;

/**
 * Registry for BasicType instances.  Lookup is primarily done by Java type
 * (Class), but can be adjusted by JDBC type-code and/or MutabilityPlan.
 * <p/>
 * It is important to understand that all basic types have a Java type.  We
 * do not support alternate EntityModes for basic-types.
 * <p/>
 * The ability
 *
 * @author Steve Ebersole
 * @author Chris Cranford
 */
@Incubating
public class BasicTypeRegistry {
	private final TypeConfiguration typeConfiguration;

	private final Map<Key,BasicType> registrations = new ConcurrentHashMap<>();
	private Map<String, Key> castTypeToKeyXref = new ConcurrentHashMap<>();
	private final Map<Class,Key> javaTypeToKeyXref = new ConcurrentHashMap<>();
	private final Map<TemporalTypeXrefKey, Key> temporalTypeToKeyXref = new ConcurrentHashMap<>();

	private final JdbcRecommendedSqlTypeMappingContext baseJdbcRecommendedSqlTypeMappingContext;

	public BasicTypeRegistry(TypeConfiguration typeConfiguration) {
		this.typeConfiguration = typeConfiguration;
		this.baseJdbcRecommendedSqlTypeMappingContext = new JdbcRecommendedSqlTypeMappingContext() {
			@Override
			public boolean isNationalized() {
				return false;
			}

			@Override
			public boolean isLob() {
				return false;
			}

			@Override
			public EnumType getEnumeratedType() {
				return EnumType.STRING;
			}

			@Override
			public TypeConfiguration getTypeConfiguration() {
				return typeConfiguration;
			}
		};
		registerBasicTypes();
	}

	public TypeConfiguration getTypeConfiguration() {
		return typeConfiguration;
	}

	public JdbcRecommendedSqlTypeMappingContext getBaseJdbcRecommendedSqlTypeMappingContext() {
		return baseJdbcRecommendedSqlTypeMappingContext;
	}


	/**
	 * Returns the default BasicType for the given Java type
	 *
	 * @param javaType The Java type (Class) for which we want the BasicType.
	 *
	 * @return The linked BasicType.  May return {@code null}
	 */
	@SuppressWarnings("unchecked")
	public <T> BasicType<T> getBasicType(Class<T> javaType) {
		final Key registryKey = javaTypeToKeyXref.get( javaType );
		if ( registryKey == null ) {
			return null;
		}

		return getBasicType( registryKey );
	}

	/**
	 * Returns the BasicType by its registryKey (pk).
	 *
	 * @param registryKey The key (id/pk) for the BasicType we want.
	 *
	 * @return The linked BasicType.  May return {@code null}
	 */
	@SuppressWarnings("unchecked")
	public <T> BasicType<T> getBasicType(Key registryKey) {
		return registrations.get( registryKey );
	}



















	@SuppressWarnings("unchecked")
	public <T> BasicType<T> resolveBasicType(
			BasicTypeParameters<T> parameters,
			JdbcRecommendedSqlTypeMappingContext jdbcTypeResolutionContext) {
		if ( parameters == null ) {
			throw new IllegalArgumentException( "BasicTypeParameters must not be null" );
		}

		// IMPL NOTE : resolving a BasicType follows very different algorithms based on what
		// specific information is available (non-null) from the BasicTypeParameters.  To help
		// facilitate that, we try to break this down into a number of sub-methods for some
		// high-level differences

		// todo implement this.  But the intention has changed.  Here we simply have another potential
		//		hint as to the SqlTypeDescriptor to use.

//		if ( parameters.getAttributeConverterDefinition() != null ) {
//			return resolveConvertedBasicType( parameters, jdbcTypeResolutionContext );
//		}


		BasicJavaTypeDescriptor<T> javaTypeDescriptor = parameters.getJavaTypeDescriptor();
		SqlTypeDescriptor sqlTypeDescriptor = parameters.getSqlTypeDescriptor();

		if ( javaTypeDescriptor == null ) {
			if ( sqlTypeDescriptor == null ) {
				throw new IllegalArgumentException( "BasicTypeParameters must define either a JavaTypeDescriptor or a SqlTypeDescriptor (if not providing AttributeConverter)" );
			}
			javaTypeDescriptor = sqlTypeDescriptor.getJdbcRecommendedJavaTypeMapping( jdbcTypeResolutionContext.getTypeConfiguration() );
		}

		if ( sqlTypeDescriptor == null ) {
			sqlTypeDescriptor = javaTypeDescriptor.getJdbcRecommendedSqlType( jdbcTypeResolutionContext );
		}

		MutabilityPlan<T> mutabilityPlan = parameters.getMutabilityPlan();
		if ( mutabilityPlan == null ) {
			mutabilityPlan = javaTypeDescriptor.getMutabilityPlan();
		}

		Comparator comparator = parameters.getComparator();
		if ( comparator == null ) {
			if ( javaTypeDescriptor.getJavaType() != null
					&& Comparable.class.isAssignableFrom( javaTypeDescriptor.getJavaType() ) ) {
				comparator = new ComparableComparator();
			}
		}

		if ( javaTypeDescriptor instanceof TemporalJavaTypeDescriptor ) {
			final TemporalJavaTypeDescriptor temporalJavaTypeDescriptor = (TemporalJavaTypeDescriptor) javaTypeDescriptor;
			javax.persistence.TemporalType temporalPrecision = parameters.getTemporalPrecision();
			if ( temporalPrecision == null ) {
				temporalPrecision = temporalJavaTypeDescriptor.getPrecision();
			}

			TemporalTypeXrefKey fk = new TemporalTypeXrefKey( temporalJavaTypeDescriptor, temporalPrecision );
			Key key = temporalTypeToKeyXref.get( fk );
			if ( key == null ) {
				key = new Key(
						temporalJavaTypeDescriptor.getJavaType(),
						sqlTypeDescriptor.getSqlType(),
						mutabilityPlan,
						temporalPrecision
				);

				temporalTypeToKeyXref.put( fk, key );
			}

			BasicType registeredType = registrations.get( key );
			if ( registeredType == null ) {
				registeredType = makeTemporalType(
						temporalJavaTypeDescriptor,
						sqlTypeDescriptor,
						mutabilityPlan,
						comparator,
						temporalPrecision
				);
			}

			return registeredType;
		}
		else {
			final Key key = new Key(
					javaTypeDescriptor.getJavaType(),
					sqlTypeDescriptor.getSqlType(),
					mutabilityPlan,
					null
			);
			BasicType registeredType = registrations.get( key );
			if ( registeredType == null ) {
				registeredType = makeBasicType(
						javaTypeDescriptor,
						sqlTypeDescriptor,
						mutabilityPlan,
						comparator
				);
			}

			return registeredType;
		}
	}

	private <T> TemporalTypeImpl makeTemporalType(
			TemporalJavaTypeDescriptor javaTypeDescriptor,
			SqlTypeDescriptor sqlTypeDescriptor,
			MutabilityPlan<T> mutabilityPlan,
			Comparator comparator,
			javax.persistence.TemporalType temporalPrecision) {
		final TemporalTypeImpl type = new TemporalTypeImpl(
				javaTypeDescriptor.getTypeName(),
				javaTypeDescriptor,
				mutabilityPlan,
				comparator,
				sqlTypeDescriptor,
				temporalPrecision
		);

		register( type, type.getRegistryKey() );

		return type;
	}

	private <T> BasicTypeImpl makeBasicType(
			BasicJavaTypeDescriptor<T> javaTypeDescriptor,
			SqlTypeDescriptor sqlTypeDescriptor,
			MutabilityPlan<T> mutabilityPlan,
			Comparator comparator) {
		final BasicTypeImpl basicType = new BasicTypeImpl(
				javaTypeDescriptor.getTypeName(),
				javaTypeDescriptor,
				mutabilityPlan,
				comparator,
				sqlTypeDescriptor
		);

		register( basicType, basicType.getRegistryKey() );

		return basicType;
	}

	public void register(BasicType type, Key registryKey) {
		if ( registryKey == null ) {
			throw new HibernateException( "Cannot register a type with a null registry key." );
		}
		if ( type == null ) {
			throw new HibernateException( "Cannot register a null type." );
		}
		if ( type.getJavaType() == null ) {
			throw new HibernateException( "BasicType must have a Java type." );
		}

		registrations.put( type.getRegistryKey(), type );
		castTypeToKeyXref.put( type.getJavaType().getSimpleName(), type.getRegistryKey() );
		javaTypeToKeyXref.put( type.getJavaType(), type.getRegistryKey() );
	}

	@SuppressWarnings("unchecked")
	private void registerBasicTypes() {
		registerBasicType( BooleanJavaDescriptor.INSTANCE, BooleanSqlDescriptor.INSTANCE );
		registerBasicType( IntegerJavaDescriptor.INSTANCE, IntegerSqlDescriptor.INSTANCE );
		registerBasicType( new BooleanJavaDescriptor( 'T', 'F' ), CharSqlDescriptor.INSTANCE );
		registerBasicType( BooleanJavaDescriptor.INSTANCE, CharSqlDescriptor.INSTANCE );

		registerBasicType( ByteJavaDescriptor.INSTANCE, TinyIntSqlDescriptor.INSTANCE );
		registerBasicType( CharacterJavaDescriptor.INSTANCE, CharSqlDescriptor.INSTANCE );
		registerBasicType( ShortJavaDescriptor.INSTANCE, SmallIntSqlDescriptor.INSTANCE );
		registerBasicType( IntegerJavaDescriptor.INSTANCE, IntegerSqlDescriptor.INSTANCE );
		registerBasicType( LongJavaDescriptor.INSTANCE, BigIntSqlDescriptor.INSTANCE );
		registerBasicType( FloatJavaDescriptor.INSTANCE, FloatSqlDescriptor.INSTANCE );
		registerBasicType( DoubleJavaDescriptor.INSTANCE, DoubleSqlDescriptor.INSTANCE );
		registerBasicType( BigDecimalJavaDescriptor.INSTANCE, NumericSqlDescriptor.INSTANCE );
		registerBasicType( BigIntegerJavaDescriptor.INSTANCE, NumericSqlDescriptor.INSTANCE );

		registerBasicType( StringJavaDescriptor.INSTANCE, VarcharSqlDescriptor.INSTANCE );
		registerBasicType( StringJavaDescriptor.INSTANCE, NVarcharSqlDescriptor.INSTANCE );
		registerBasicType( CharacterJavaDescriptor.INSTANCE, NCharSqlDescriptor.INSTANCE );
		registerBasicType( UrlJavaDescriptor.INSTANCE, VarcharSqlDescriptor.INSTANCE );

		registerBasicType( DurationJavaDescriptor.INSTANCE, BigIntSqlDescriptor.INSTANCE );

		registerTemporalType( InstantJavaDescriptor.INSTANCE, TimestampSqlDescriptor.INSTANCE );
		registerTemporalType( LocalDateTimeJavaDescriptor.INSTANCE, TimestampSqlDescriptor.INSTANCE );
		registerTemporalType( LocalDateJavaDescriptor.INSTANCE, DateSqlDescriptor.INSTANCE );
		registerTemporalType( LocalTimeJavaDescriptor.INSTANCE, TimeSqlDescriptor.INSTANCE );
		registerTemporalType( OffsetDateTimeJavaDescriptor.INSTANCE, TimestampSqlDescriptor.INSTANCE );
		registerTemporalType( OffsetTimeJavaDescriptor.INSTANCE, TimeSqlDescriptor.INSTANCE );
		registerTemporalType( ZonedDateTimeJavaDescriptor.INSTANCE, TimestampSqlDescriptor.INSTANCE );

		registerTemporalType( JdbcDateJavaDescriptor.INSTANCE, DateSqlDescriptor.INSTANCE );
		registerTemporalType( JdbcTimeJavaDescriptor.INSTANCE, TimeSqlDescriptor.INSTANCE );
		registerTemporalType( JdbcTimestampJavaDescriptor.INSTANCE, TimestampSqlDescriptor.INSTANCE );
		registerTemporalType( CalendarTimeJavaDescriptor.INSTANCE, TimestampSqlDescriptor.INSTANCE );
		registerTemporalType( CalendarDateJavaDescriptor.INSTANCE, DateSqlDescriptor.INSTANCE );

		registerBasicType( LocaleJavaDescriptor.INSTANCE, VarcharSqlDescriptor.INSTANCE );
		registerBasicType( CurrencyJavaDescriptor.INSTANCE, VarcharSqlDescriptor.INSTANCE );
		registerBasicType( TimeZoneJavaDescriptor.INSTANCE, VarcharSqlDescriptor.INSTANCE );
		registerBasicType( ClassJavaDescriptor.INSTANCE, VarcharSqlDescriptor.INSTANCE );
		registerBasicType( UUIDJavaDescriptor.INSTANCE, BinarySqlDescriptor.INSTANCE );
		registerBasicType( UUIDJavaDescriptor.INSTANCE, VarcharSqlDescriptor.INSTANCE );

		registerBasicType( PrimitiveByteArrayJavaDescriptor.INSTANCE, VarbinarySqlDescriptor.INSTANCE );
		registerBasicType( ByteArrayJavaDescriptor.INSTANCE, VarbinarySqlDescriptor.INSTANCE );
		registerBasicType( PrimitiveByteArrayJavaDescriptor.INSTANCE, LongVarbinarySqlDescriptor.INSTANCE );
		registerBasicType( PrimitiveCharacterArrayJavaDescriptor.INSTANCE, VarcharSqlDescriptor.INSTANCE );
		registerBasicType( CharacterArrayJavaDescriptor.INSTANCE, VarcharSqlDescriptor.INSTANCE );
		registerBasicType( StringJavaDescriptor.INSTANCE, LongVarcharSqlDescriptor.INSTANCE );
		registerBasicType( StringJavaDescriptor.INSTANCE, LongNVarcharSqlDescriptor.INSTANCE );
		registerBasicType( BlobJavaDescriptor.INSTANCE, BlobSqlDescriptor.DEFAULT );
		registerBasicType( PrimitiveByteArrayJavaDescriptor.INSTANCE, BlobSqlDescriptor.DEFAULT );
		registerBasicType( ClobJavaDescriptor.INSTANCE, ClobSqlDescriptor.DEFAULT );
		registerBasicType( NClobJavaDescriptor.INSTANCE, NClobSqlDescriptor.DEFAULT );
		registerBasicType( StringJavaDescriptor.INSTANCE, ClobSqlDescriptor.DEFAULT );
		registerBasicType( StringJavaDescriptor.INSTANCE, NClobSqlDescriptor.DEFAULT );
		registerBasicType( SerializableJavaDescriptor.INSTANCE, VarbinarySqlDescriptor.INSTANCE );

		// todo : ObjectType
		// composed of these two types.
		// StringType.INSTANCE = ( StringTypeDescriptor.INSTANCE, VarcharTypeDescriptor.INSTANCE )
		// SerializableType.INSTANCE = ( SerializableTypeDescriptor.INSTANCE, VarbinaryTypeDescriptor.INSTANCE )
		// based on AnyType

		// Immutable types
		registerTemporalType( JdbcDateJavaDescriptor.INSTANCE, DateSqlDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
		registerTemporalType( JdbcTimeJavaDescriptor.INSTANCE, TimeSqlDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
		registerTemporalType( JdbcTimestampJavaDescriptor.INSTANCE, TimestampSqlDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
		registerTemporalType( CalendarTimeJavaDescriptor.INSTANCE, TimestampSqlDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
		registerTemporalType( CalendarDateJavaDescriptor.INSTANCE, DateSqlDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
		registerBasicType( PrimitiveByteArrayJavaDescriptor.INSTANCE, VarbinarySqlDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
		registerBasicType( SerializableJavaDescriptor.INSTANCE, VarbinarySqlDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
	}

	private void registerBasicType(BasicJavaTypeDescriptor javaTypeDescriptor, SqlTypeDescriptor sqlTypeDescriptor) {
		registerBasicType( javaTypeDescriptor, sqlTypeDescriptor, null );
	}

	@SuppressWarnings("unchecked")
	private void registerBasicType(
			BasicJavaTypeDescriptor javaDescriptor,
			SqlTypeDescriptor sqlDescriptor,
			MutabilityPlan mutabilityPlan) {
		assert javaDescriptor != null;
		assert sqlDescriptor != null;

		final BasicType type = new BasicTypeImpl(
				"BasicTypeImpl(" +
						javaDescriptor.getTypeName() + ", " +
						sqlDescriptor.getSqlType() + ", " +
						(mutabilityPlan == null ? "<no-mutability-plan>" : mutabilityPlan.getClass().getSimpleName() ) + ")",
				javaDescriptor,
				mutabilityPlan,
				null,
				sqlDescriptor
		);
		register( type, type.getRegistryKey() );
	}

	private void registerTemporalType(TemporalJavaTypeDescriptor javaDescriptor, SqlTypeDescriptor sqlDescriptor) {
		registerTemporalType( javaDescriptor, sqlDescriptor, null );
	}

	@SuppressWarnings("unchecked")
	private void registerTemporalType(
			TemporalJavaTypeDescriptor javaDescriptor,
			SqlTypeDescriptor sqlDescriptor,
			MutabilityPlan mutabilityPlan) {
		final TemporalType type = new TemporalTypeImpl(
				"TemporalTypeImpl( " +
						javaDescriptor.getTypeName() + ", " +
						sqlDescriptor.getSqlType() + ", " +
						(mutabilityPlan == null ? "<no-mutability-plan>" : mutabilityPlan.getClass().getSimpleName() ) +
						javaDescriptor.getPrecision().name() + ")",

				javaDescriptor,
				mutabilityPlan,
				null,
				sqlDescriptor,
				javaDescriptor.getPrecision()
		);
		register( type, type.getRegistryKey() );
	}

	public BasicType getBasicTypeForCast(String name) {
		final Key key = castTypeToKeyXref.get( name );
		if ( key == null ) {
			throw new IllegalArgumentException( "Could not determine cast type for given name : " + name );
		}

		return registrations.get( key );
	}

	/**
	 * Represents a "primary key" into the registrations
	 */
	public static class Key {
		private final Class javaType;
		private final int jdbcTypeCode;
		private final MutabilityPlan mutabilityPlan;
		private final javax.persistence.TemporalType temporalPrecision;

		private Key(
				Class javaType,
				int jdbcTypeCode,
				MutabilityPlan mutabilityPlan,
				javax.persistence.TemporalType temporalPrecision) {
			this.javaType = javaType;
			this.jdbcTypeCode = jdbcTypeCode;
			this.mutabilityPlan = mutabilityPlan;
			this.temporalPrecision = temporalPrecision;
		}

		public Class getJavaType() {
			return javaType;
		}

		public int getJdbcTypeCode() {
			return jdbcTypeCode;
		}

		public MutabilityPlan getMutabilityPlan() {
			return mutabilityPlan;
		}

		public javax.persistence.TemporalType getTemporalPrecision() {
			return temporalPrecision;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			Key key = (Key) o;

			if ( getJdbcTypeCode() != key.getJdbcTypeCode() ) {
				return false;
			}
			if ( !getJavaType().equals( key.getJavaType() ) ) {
				return false;
			}
			if ( !getMutabilityPlan().equals( key.getMutabilityPlan() ) ) {
				return false;
			}
			return getTemporalPrecision() == key.getTemporalPrecision();
		}

		@Override
		public int hashCode() {
			int result = getJavaType().hashCode();
			result = 31 * result + getJdbcTypeCode();
			result = 31 * result + getMutabilityPlan().hashCode();
			result = 31 * result + ( getTemporalPrecision() != null ? getTemporalPrecision().hashCode() : 0 );
			return result;
		}

		public static Key from(BasicType type) {
			javax.persistence.TemporalType precision = null;
			if ( type instanceof TemporalType ) {
				precision = ( (TemporalType) type ).getPrecision();
			}
			return from(
					(TemporalJavaTypeDescriptor) type.getJavaTypeDescriptor(),
					type.getColumnMappings()[0].getSqlTypeDescriptor(),
					type.getMutabilityPlan(),
					precision
			);
		}

		private static Key from(
				BasicJavaTypeDescriptor javaTypeDescriptor,
				SqlTypeDescriptor sqlTypeDescriptor,
				MutabilityPlan mutabilityPlan,
				javax.persistence.TemporalType temporalPrecision) {
			return new Key(
					javaTypeDescriptor.getJavaType(),
					sqlTypeDescriptor == null ? Types.OTHER : sqlTypeDescriptor.getSqlType(),
					mutabilityPlan,
					temporalPrecision
			);
		}

		public static Key from(
				BasicJavaTypeDescriptor javaTypeDescriptor,
				SqlTypeDescriptor sqlTypeDescriptor) {
			return from(
					javaTypeDescriptor,
					sqlTypeDescriptor,
					javaTypeDescriptor.getMutabilityPlan(),
					javaTypeDescriptor instanceof TemporalJavaTypeDescriptor
							? ( (TemporalJavaTypeDescriptor) javaTypeDescriptor ).getPrecision()
							: null
			);
		}
	}

	public static class TemporalTypeXrefKey {
		private final TemporalJavaTypeDescriptor javaTypeDescriptor;
		private final javax.persistence.TemporalType precision;

		public TemporalTypeXrefKey(TemporalJavaTypeDescriptor javaTypeDescriptor, javax.persistence.TemporalType precision) {
			this.javaTypeDescriptor = javaTypeDescriptor;
			this.precision = precision;
		}

		public TemporalJavaTypeDescriptor getJavaTypeDescriptor() {
			return javaTypeDescriptor;
		}

		public javax.persistence.TemporalType getPrecision() {
			return precision;
		}
	}
}
