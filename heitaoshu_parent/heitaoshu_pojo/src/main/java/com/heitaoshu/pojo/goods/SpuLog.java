package com.heitaoshu.pojo.goods;

import java.io.Serializable;
import java.util.Date;

public class SpuLog implements Serializable {
    private Integer id;
    private String oldInfo;
    private String newInfo;
    private String user;
    private Date date;
}
