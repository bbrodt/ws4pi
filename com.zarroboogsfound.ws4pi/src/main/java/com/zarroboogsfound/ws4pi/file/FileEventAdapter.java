package com.zarroboogsfound.ws4pi.file;

public abstract class FileEventAdapter implements FileListener {
	  @Override
	  public void onCreated(FileEvent event) {
	    // no implementation provided
	  }

	  @Override
	  public void onModified(FileEvent event) {
	   // no implementation provided
	  }

	  @Override
	  public void onDeleted(FileEvent event) {
	   // no implementation provided
	  }
	}