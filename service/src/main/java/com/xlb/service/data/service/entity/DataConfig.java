package com.xlb.service.data.service.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "single_data_config")
public class DataConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "data_id")
    private Integer dataId;

    private String keyword;

    private String value;

}
