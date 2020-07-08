package com.xlb.service.data.service.entity;

import com.xlb.base.entity.DFlagEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "single_data_info")
public class DataInfo extends DFlagEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String keyword;

    private String type;

    private String data;
}
