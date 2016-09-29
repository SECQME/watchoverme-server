package com.secqme.domain.dao;

import java.io.Serializable;


public interface BaseDAO<T, PK extends Serializable> {
   public void create(T t);
   public T read(PK pk);
   public void update(T t);
   public void delete(T t);
   public T refresh(T t);

}
