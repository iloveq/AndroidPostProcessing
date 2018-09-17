package com.woaiqw.orm.db.sqlite;

import java.util.HashMap;
import java.util.Map.Entry;




import android.database.Cursor;

import com.woaiqw.orm.FinalDb;
import com.woaiqw.orm.db.table.ManyToOne;
import com.woaiqw.orm.db.table.OneToMany;
import com.woaiqw.orm.db.table.Property;
import com.woaiqw.orm.db.table.TableInfo;

public class CursorUtils {

	public static <T> T getEntity(Cursor cursor, Class<T> clazz,FinalDb db){
		try {
			if(cursor!=null ){
				TableInfo table = TableInfo.get(clazz);
				int columnCount = cursor.getColumnCount();
				if(columnCount>0){
					T  entity = (T) clazz.newInstance();
					for(int i=0;i<columnCount;i++){
						
						String column = cursor.getColumnName(i);
						
						Property property = table.propertyMap.get(column);
						if(property!=null){
							property.setValue(entity, cursor.getString(i));
						}else{
							if(table.getId().getColumn().equals(column)){
								table.getId().setValue(entity,  cursor.getString(i));
							}
						}

					}
                    /**
                     * 处理OneToMany的lazyLoad形式
                     */
                    for(OneToMany oneToManyProp : table.oneToManyMap.values()){
                        if(oneToManyProp.getDataType()==OneToManyLazyLoader.class){
                            OneToManyLazyLoader oneToManyLazyLoader = new OneToManyLazyLoader(entity,clazz,oneToManyProp.getOneClass(),db);
                            oneToManyProp.setValue(entity,oneToManyLazyLoader);
                        }
                    }

                    /**
                     * 处理ManyToOne的lazyLoad形式
                     */
                    for(ManyToOne manyToOneProp : table.manyToOneMap.values()){
                        if(manyToOneProp.getDataType()==ManyToOneLazyLoader.class){
                            ManyToOneLazyLoader manyToOneLazyLoader = new ManyToOneLazyLoader(entity,clazz,manyToOneProp.getManyClass(),db);
                            manyToOneLazyLoader.setFieldValue(cursor.getInt(cursor.getColumnIndex(manyToOneProp.getColumn())));
                            manyToOneProp.setValue(entity,manyToOneLazyLoader);
                        }
                    }
					return entity;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static DbModel getDbModel(Cursor cursor){
		if(cursor!=null && cursor.getColumnCount() > 0){
			DbModel model = new DbModel();
			int columnCount = cursor.getColumnCount();
			for(int i=0;i<columnCount;i++){
				model.set(cursor.getColumnName(i), cursor.getString(i));
			}
			return model;
		}
		return null;
	}
	
	
	public static <T> T dbModel2Entity(DbModel dbModel,Class<?> clazz){
		if(dbModel!=null){
			HashMap<String, Object> dataMap = dbModel.getDataMap();
			try {
				@SuppressWarnings("unchecked")
				T  entity = (T) clazz.newInstance();
				for(Entry<String, Object> entry : dataMap.entrySet()){
					String column = entry.getKey();
					TableInfo table = TableInfo.get(clazz);
					Property property = table.propertyMap.get(column);
					if(property!=null){
						property.setValue(entity, entry.getValue()==null?null:entry.getValue().toString());
					}else{
						if(table.getId().getColumn().equals(column)){
							table.getId().setValue(entity, entry.getValue()==null?null:entry.getValue().toString());
						}
					}
					
				}
				return entity;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	
}
