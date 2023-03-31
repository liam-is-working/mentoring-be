package com.example.mentoringapis.entities;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public enum Gender implements Serializable, UserType {
    male, female, others;

    @Override
    public int getSqlType() {
        return 0;
    }

    @Override
    public Class returnedClass() {
        return null;
    }

    @Override
    public boolean equals(Object o, Object j1) {
        return false;
    }

    @Override
    public int hashCode(Object o) {
        return 0;
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, int i, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws SQLException {
        return null;
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i, SharedSessionContractImplementor sharedSessionContractImplementor) throws SQLException {

    }

    @Override
    public Object deepCopy(Object o) {
        return null;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object o) {
        return null;
    }

    @Override
    public Object assemble(Serializable serializable, Object o) {
        return null;
    }

    @Override
    public Object replace(Object o, Object j1, Object o2) {
        return null;
    }
}
