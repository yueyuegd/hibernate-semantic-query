/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.spi;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Contract for binding values to a {@link PreparedStatement}.
 *
 * @author Steve Ebersole
 */
public interface JdbcValueBinder<X> {
	/**
	 * Bind a value to a prepared statement.
	 *
	 * @param st The prepared statement to which to bind the value.
	 * @param value The value to bind.
	 * @param index The position at which to bind the value within the prepared statement
	 * @param options The options.
	 *
	 * @throws SQLException Indicates a JDBC error occurred.
	 */
	void bind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException;

	/**
	 * Bind a value to a CallableStatement.
	 *
	 * @param st The prepared statement to which to bind the value.
	 * @param value The value to bind.
	 * @param name The name to bind the value within the prepared statement
	 * @param options The options.
	 *
	 * @throws SQLException Indicates a JDBC error occurred.
	 */
	void bind(CallableStatement st, X value, String name, WrapperOptions options) throws SQLException;
}
