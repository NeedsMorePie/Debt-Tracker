package com.example.daviswu.debttracker;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Davis Wu on 2015-01-13.
 */
public class Item implements Serializable {
    protected String itemOwed = "";
    protected String status = "";
    protected String name = "";
    protected String email = "";
    protected String phone = "";
    protected File imgPath = null;
    protected int fileNumber = -1;
}
