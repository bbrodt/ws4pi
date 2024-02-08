package com.zarroboogsfound.ws4pi.file;

import java.util.EventListener;

public interface FileListener extends EventListener {
    public void onCreated(FileEvent event);
    public void onModified(FileEvent event);
    public void onDeleted(FileEvent event);
}