package com.irrigation.erp.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "material_distribution_table")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDistributionTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String Est_No;
    private String S_I_No;
    private String NatureOfWork;
    private String Qn_Used;
    private String QuantityOfWorkDone;
    private String M_B_No_And_Folio;
    private String Remarks;
}
