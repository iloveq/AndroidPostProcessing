package com.woaiqw.orm.exception;

public class DbException extends AfinalException {
	private static final long serialVersionUID = 1L;
	
	public DbException() {}
	
	
	public DbException(String msg) {
		super(msg);
	}
	
	public DbException(Throwable ex) {
		super(ex);
	}
	
	public DbException(String msg,Throwable ex) {
		super(msg,ex);
	}
	
}
