-- V68: 공통코드(common_code) 테이블 + FMS·BMS·PMS 전용 enum 시드.
-- 그룹 수: FMS 29 + BMS 4 + PMS 전용 3 = 36개 그룹
-- 코드 행 수: 약 233개 (아래 시드 포함)
-- group_code = EnumRegistryFactory 등록 키, source_module = 원천 모듈
-- code = enum.name(), label/label_ko = EnumOption 필드와 동일

-- ========== 1. DDL ==========

CREATE TABLE IF NOT EXISTS admin.common_code_group (
    common_code_group_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    group_code           VARCHAR(80)  NOT NULL,
    source_module        VARCHAR(10)  NOT NULL,
    description          VARCHAR(200),
    active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by           VARCHAR(50),
    updated_by           VARCHAR(50),
    CONSTRAINT uq_admin_common_code_group_code UNIQUE (group_code)
);

CREATE INDEX IF NOT EXISTS ix_admin_common_code_group_module ON admin.common_code_group(source_module, active);

CREATE TABLE IF NOT EXISTS admin.common_code (
    common_code_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    group_code     VARCHAR(80)  NOT NULL REFERENCES admin.common_code_group(group_code),
    code           VARCHAR(80)  NOT NULL,
    label          VARCHAR(200) NOT NULL,
    label_ko       VARCHAR(200),
    sort_order     INT          NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(50),
    updated_by     VARCHAR(50),
    CONSTRAINT uq_admin_common_code_group_code_code UNIQUE (group_code, code)
);

CREATE INDEX IF NOT EXISTS ix_admin_common_code_group_active ON admin.common_code(group_code, active, sort_order);

-- ========== 2. 그룹 시드 (FMS 29 + BMS 4 + PMS전용 3 = 36개) ==========

INSERT INTO admin.common_code_group (group_code, source_module, description) VALUES

    -- FMS common enums
    ('Per',                    'FMS', 'Rate per unit'),
    ('Bound',                  'FMS', 'Export/Import direction'),
    ('BlType',                 'FMS', 'Bill of Lading type'),
    ('FreightTerm',            'FMS', 'Freight payment term'),
    ('Incoterms',              'FMS', 'International commercial terms'),
    ('ServiceTerm',            'FMS', 'Service term (CY/CFS etc.)'),
    ('RateClass',              'FMS', 'Air cargo rate class'),
    ('WeightUnit',             'FMS', 'Weight unit'),
    ('SecurityStatus',         'FMS', 'Cargo security status'),
    ('LoadType',               'FMS', 'Load type (FCL/LCL/BULK)'),
    ('ShipmentType',           'FMS', 'Shipment type (House/Direct)'),
    ('WorkDivision',           'FMS', 'Work division (Sea/Air/Warehouse/Trucking)'),
    ('SortDirection',          'FMS', 'Sort direction'),
    ('VolumeDivisor',          'FMS', 'Volume weight divisor'),
    ('DescClause1',            'FMS', 'Description clause 1 (Shipper''s load)'),
    ('DescClause2',            'FMS', 'Description clause 2 (Said to contain)'),
    ('FreightCondition',       'FMS', 'Freight condition (Prepaid/Collect)'),

    -- FMS housebl enums
    ('Fhd',                    'FMS', 'F.H.D. (door delivery) type'),
    ('FlightType',             'FMS', 'Flight type (Passenger/Cargo)'),
    ('CargoType',              'FMS', 'Cargo type (Normal/Danger/Cool etc.)'),
    ('HandlingInfoCode',       'FMS', 'Handling information code'),
    ('ContainerType',          'FMS', 'ISO container type'),
    ('TruckType',              'FMS', 'Truck type by tonnage'),
    ('SalesClass',             'FMS', 'Sales class (Sales/Nomi)'),
    ('NoOfBl',                 'FMS', 'Number of original B/L'),
    ('housebl.JobDiv',         'FMS', 'House B/L job division (Sea/Air/Truck/Non B/L)'),

    -- FMS masterbl enums
    ('masterbl.MasterBlJobDiv','FMS', 'Master B/L job division (Sea/Air)'),

    -- FMS freight enums
    ('TaxType',                'FMS', 'Tax type (Taxable/Zero-rated/Exempt)'),
    ('FinancialDocType',       'FMS', 'Financial document type (Invoice/Payment/Debit/Credit)'),

    -- BMS enums
    ('DocumentStatus',         'BMS', 'Financial document status'),
    ('DocumentType',           'BMS', 'Financial document type'),
    ('GroupCategory',          'BMS', 'Document group category'),
    ('IssueType',              'BMS', 'Issue type (Tax/Slip)'),

    -- PMS-only enums (FMS/BMS에 없는 그룹)
    ('AggregationBasis',       'PMS', 'PMS aggregation basis'),
    ('DateKind',               'PMS', 'PMS date filter kind'),
    ('PortKind',               'PMS', 'PMS port filter kind')

ON CONFLICT (group_code) DO NOTHING;

-- ========== 3. 코드 시드 ==========

-- Per (FMS: code=getCode(), label=getDescription())
-- EnumRegistryFactory: new EnumOption(e.getCode(), e.getDescription(), e.getDescription())
-- code = enum.getCode(), label = description
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('Per', 'SHP',  'Ship',  NULL, 0),
    ('Per', 'BL',   'B/L',   NULL, 1),
    ('Per', 'CNTR', 'CNTR',  NULL, 2),
    ('Per', 'RT',   'R/TON', NULL, 3),
    ('Per', 'CB',   'CBM',   NULL, 4),
    ('Per', 'OT',   'OTH',   NULL, 5),
    ('Per', 'CW',   'C/WT',  NULL, 6),
    ('Per', 'GW',   'G/WT',  NULL, 7),
    ('Per', 'MIN',  'MIN',   NULL, 8),
    ('Per', 'UNIT', 'UNIT',  NULL, 9),
    ('Per', 'SET',  'SET',   NULL, 10),
    ('Per', 'QTY',  'QTY',   NULL, 11),
    ('Per', 'TRK',  'Truck', NULL, 12),
    ('Per', 'TRP',  'Trip',  NULL, 13),
    ('Per', 'RM',   'Norm',  NULL, 14),
    ('Per', 'M2',   'M2',    NULL, 15),
    ('Per', 'LIT',  'Lit',   NULL, 16),
    ('Per', 'TN',   'Ton',   NULL, 17)
ON CONFLICT (group_code, code) DO NOTHING;

-- Bound (code=name(), label=label, labelKo=labelKo)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('Bound', 'EXP', 'Export', '수출', 0),
    ('Bound', 'IMP', 'Import', '수입', 1)
ON CONFLICT (group_code, code) DO NOTHING;

-- BlType (code=name(), label=label, labelKo=null)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('BlType', 'ORIGINAL',  'Original',    NULL, 0),
    ('BlType', 'SURRENDER', 'Surrender',   NULL, 1),
    ('BlType', 'SEAWAY',    'Sea-Way Bill', NULL, 2),
    ('BlType', 'NORMAL',    'Normal',      NULL, 3),
    ('BlType', 'EXPRESS',   'Express',     NULL, 4)
ON CONFLICT (group_code, code) DO NOTHING;

-- FreightTerm (code=name(), label=label, labelKo=labelKo)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('FreightTerm', 'PREPAID', 'Prepaid', '선불', 0),
    ('FreightTerm', 'COLLECT', 'Collect', '후불', 1)
ON CONFLICT (group_code, code) DO NOTHING;

-- Incoterms (code=name(), label=label, labelKo=null)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('Incoterms', 'EXW', 'EXW', NULL, 0),
    ('Incoterms', 'FCA', 'FCA', NULL, 1),
    ('Incoterms', 'CPT', 'CPT', NULL, 2),
    ('Incoterms', 'CIP', 'CIP', NULL, 3),
    ('Incoterms', 'DAP', 'DAP', NULL, 4),
    ('Incoterms', 'DPU', 'DPU', NULL, 5),
    ('Incoterms', 'DDP', 'DDP', NULL, 6),
    ('Incoterms', 'FAS', 'FAS', NULL, 7),
    ('Incoterms', 'FOB', 'FOB', NULL, 8),
    ('Incoterms', 'CFR', 'CFR', NULL, 9),
    ('Incoterms', 'CIF', 'CIF', NULL, 10)
ON CONFLICT (group_code, code) DO NOTHING;

-- ServiceTerm (code=name(), label=label, labelKo=null)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('ServiceTerm', 'CY_CY',      'CY/CY',      NULL, 0),
    ('ServiceTerm', 'CY_CFS',     'CY/CFS',     NULL, 1),
    ('ServiceTerm', 'CFS_CFS',    'CFS/CFS',    NULL, 2),
    ('ServiceTerm', 'CFS_CY',     'CFS/CY',     NULL, 3),
    ('ServiceTerm', 'BULK',       'BULK',       NULL, 4),
    ('ServiceTerm', 'FIOS',       'F.I.O.S.',   NULL, 5),
    ('ServiceTerm', 'FIFO',       'F.I.F.O.',   NULL, 6),
    ('ServiceTerm', 'FIBT',       'F.I.B.T.',   NULL, 7),
    ('ServiceTerm', 'BT_BT',      'BT/BT',      NULL, 8),
    ('ServiceTerm', 'DOOR_DOOR',  'DOOR/DOOR',  NULL, 9),
    ('ServiceTerm', 'CY_TK',      'CY/TK',      NULL, 10),
    ('ServiceTerm', 'TK_TK',      'TK/TK',      NULL, 11),
    ('ServiceTerm', 'TK_CY',      'TK/CY',      NULL, 12),
    ('ServiceTerm', 'CY_DOOR',    'CY/DOOR',    NULL, 13),
    ('ServiceTerm', 'BERTH_BERTH','BERTH/BERTH', NULL, 14),
    ('ServiceTerm', 'RO_RO',      'RO-RO',      NULL, 15),
    ('ServiceTerm', 'CFS_DOOR',   'CFS/DOOR',   NULL, 16),
    ('ServiceTerm', 'DOOR_CY',    'DOOR/CY',    NULL, 17),
    ('ServiceTerm', 'DOOR_CFS',   'DOOR/CFS',   NULL, 18),
    ('ServiceTerm', 'CY_FO',      'CY/FO',      NULL, 19),
    ('ServiceTerm', 'FILO',       'F.I.L.O.',   NULL, 20),
    ('ServiceTerm', 'CY_RAMP',    'CY/RAMP',    NULL, 21),
    ('ServiceTerm', 'DOOR_TML',   'DOOR/TML',   NULL, 22),
    ('ServiceTerm', 'CY_TML',     'CY/TML',     NULL, 23),
    ('ServiceTerm', 'TML_TML',    'TML/TML',    NULL, 24),
    ('ServiceTerm', 'TML_CY',     'TML/CY',     NULL, 25),
    ('ServiceTerm', 'TML_DOOR',   'TML/DOOR',   NULL, 26)
ON CONFLICT (group_code, code) DO NOTHING;

-- RateClass (code=name(), label=label)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('RateClass', 'M', 'M', NULL, 0),
    ('RateClass', 'N', 'N', NULL, 1),
    ('RateClass', 'Q', 'Q', NULL, 2),
    ('RateClass', 'C', 'C', NULL, 3)
ON CONFLICT (group_code, code) DO NOTHING;

-- WeightUnit (code=name(), label=label)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('WeightUnit', 'KGS', 'KGS', NULL, 0),
    ('WeightUnit', 'LBS', 'LBS', NULL, 1)
ON CONFLICT (group_code, code) DO NOTHING;

-- SecurityStatus (code=name(), label=label)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('SecurityStatus', 'SPX', 'SPX', NULL, 0),
    ('SecurityStatus', 'SCO', 'SCO', NULL, 1),
    ('SecurityStatus', 'UNK', 'UNK', NULL, 2)
ON CONFLICT (group_code, code) DO NOTHING;

-- LoadType (code=name(), label=label)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('LoadType', 'FCL',  'FCL',  NULL, 0),
    ('LoadType', 'LCL',  'LCL',  NULL, 1),
    ('LoadType', 'BULK', 'BULK', NULL, 2)
ON CONFLICT (group_code, code) DO NOTHING;

-- ShipmentType (code=name(), label=label)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('ShipmentType', 'HOUSE',  'House',  NULL, 0),
    ('ShipmentType', 'DIRECT', 'Direct', NULL, 1)
ON CONFLICT (group_code, code) DO NOTHING;

-- WorkDivision (code=name(), label=label, labelKo=labelKo)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('WorkDivision', 'SEA',       'Sea',       '해상',     0),
    ('WorkDivision', 'AIR',       'Air',       '항공',     1),
    ('WorkDivision', 'WAREHOUSE', 'Warehouse', '창고',     2),
    ('WorkDivision', 'TRUCKING',  'Trucking',  '육상운송', 3)
ON CONFLICT (group_code, code) DO NOTHING;

-- SortDirection (code=name(), label=label)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('SortDirection', 'ASC',  'ASC',  NULL, 0),
    ('SortDirection', 'DESC', 'DESC', NULL, 1)
ON CONFLICT (group_code, code) DO NOTHING;

-- VolumeDivisor (code=name(), label=label)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('VolumeDivisor', 'CM6000', 'CM / 6000', NULL, 0),
    ('VolumeDivisor', 'CM5000', 'CM / 5000', NULL, 1),
    ('VolumeDivisor', 'CM5500', 'CM / 5500', NULL, 2),
    ('VolumeDivisor', 'CM7000', 'CM / 7000', NULL, 3),
    ('VolumeDivisor', 'CM7500', 'CM / 7500', NULL, 4),
    ('VolumeDivisor', 'CM8000', 'CM / 8000', NULL, 5),
    ('VolumeDivisor', 'IN166',  'INCH / 166', NULL, 6),
    ('VolumeDivisor', 'IN366',  'INCH / 366', NULL, 7)
ON CONFLICT (group_code, code) DO NOTHING;

-- DescClause1 (code=name()=A..G, label=public field label)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('DescClause1', 'A', 'SHIPPER''S LOAD & COUNT',                                    NULL, 0),
    ('DescClause1', 'B', 'SHIPPER''S LOAD AND COUNT',                                  NULL, 1),
    ('DescClause1', 'C', 'SHIPPER''S LOAD, COUNT, STOW & SEAL',                        NULL, 2),
    ('DescClause1', 'D', 'SHIPPER''S LOAD & COUNT & SEALED',                           NULL, 3),
    ('DescClause1', 'E', 'SHIPPER''S RISK & DAMAGE FOR ON DECK CARGO',                 NULL, 4),
    ('DescClause1', 'F', 'SHIPPER''S LOAD, STOW, WEIGHT, COUNT & SEAL',                NULL, 5),
    ('DescClause1', 'G', 'PART OF CONTAINER',                                          NULL, 6)
ON CONFLICT (group_code, code) DO NOTHING;

-- DescClause2 (code=name()=A..C, label=public field label)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('DescClause2', 'A', 'SAID TO CONTAIN :',               NULL, 0),
    ('DescClause2', 'B', 'SAID TO BE :',                    NULL, 1),
    ('DescClause2', 'C', 'SHIPPER''S WEIGHT & MEASUREMENT', NULL, 2)
ON CONFLICT (group_code, code) DO NOTHING;

-- FreightCondition (code=name(), label=description)
-- EnumRegistryFactory: new EnumOption(e.name(), e.getDescription(), e.getDescription())
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('FreightCondition', 'P', 'Prepaid', NULL, 0),
    ('FreightCondition', 'C', 'Collect', NULL, 1)
ON CONFLICT (group_code, code) DO NOTHING;

-- Fhd (code=name(), label=description)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('Fhd', 'N', 'Not',     NULL, 0),
    ('Fhd', 'F', 'F.H.D',   NULL, 1),
    ('Fhd', 'D', 'To Door', NULL, 2)
ON CONFLICT (group_code, code) DO NOTHING;

-- FlightType (code=name(), label=description, labelKo=labelKo)
-- EnumRegistryFactory: new EnumOption(e.name(), e.getDescription(), e.getDescription(), e.getLabelKo())
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('FlightType', 'P', 'Passenger', '여객', 0),
    ('FlightType', 'C', 'Cargo',     '화물', 1)
ON CONFLICT (group_code, code) DO NOTHING;

-- CargoType (code=getCode(), label=description)
-- EnumRegistryFactory: new EnumOption(e.getCode(), e.getDescription(), e.getDescription())
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('CargoType', 'NR',   'Normal',       NULL, 0),
    ('CargoType', 'DG',   'Danger',       NULL, 1),
    ('CargoType', 'KC',   'Keep Cool',    NULL, 2),
    ('CargoType', 'KF',   'Keep Frozen',  NULL, 3),
    ('CargoType', 'OS',   'Over Size',    NULL, 4),
    ('CargoType', 'SWTW', 'S/W,T/W',     NULL, 5),
    ('CargoType', 'NS',   'Non-Stackable',NULL, 6),
    ('CargoType', 'FD',   'Food',         NULL, 7)
ON CONFLICT (group_code, code) DO NOTHING;

-- HandlingInfoCode (code=getCode(), label=description)
-- EnumRegistryFactory: new EnumOption(e.getCode(), e.getDescription(), e.getDescription())
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('HandlingInfoCode', 'A', 'ATTACHED : COMM INV & P/LIST',                                                      NULL, 0),
    ('HandlingInfoCode', 'D', 'PLS CTC CNEE IMMY UPON ARRVL',                                                      NULL, 1),
    ('HandlingInfoCode', 'E', 'DOC''S ATTACHED WITH MAWB',                                                         NULL, 2),
    ('HandlingInfoCode', 'F', 'ATTACHED : COMM INV & PACKING LIST & C/O',                                          NULL, 3),
    ('HandlingInfoCode', 'G', 'ATTACHED : COMM INV',                                                               NULL, 4),
    ('HandlingInfoCode', 'H', 'ATTACHED : COMM INV & PACKING LIST & C/O & E/P',                                    NULL, 5),
    ('HandlingInfoCode', 'I', 'ATTACHED : POUCH',                                                                  NULL, 6),
    ('HandlingInfoCode', 'J', 'INVOICE, PACKING LIST ATTD',                                                        NULL, 7),
    ('HandlingInfoCode', 'K', 'TOTAL : (  ) PACKAGES ONLY. ONE POUCH OF DOCUMENT ATTACHED.',                       NULL, 8),
    ('HandlingInfoCode', 'M', 'ATT:INVOICE & P/LIST.',                                                             NULL, 9),
    ('HandlingInfoCode', 'N', 'ATT:ENVLP',                                                                         NULL, 10),
    ('HandlingInfoCode', 'O', 'ATT:INVOICE  P/LIST DANGEROUS GOODS AS PER ATTACHED SHIPPER S DECLARATION',         NULL, 11),
    ('HandlingInfoCode', 'P', 'ATT:INVOICE  P/LIST DANGEROUS GOODS AS PER ATTACHED DGD - CARGO AIRCRAFT ONLY.',   NULL, 12),
    ('HandlingInfoCode', 'C', 'ATT.(1)ENV.,NO PARTS OF THE PACKAGE CONTENTS IS DANGEROUS',                        NULL, 13),
    ('HandlingInfoCode', 'B', 'NO ATTACHED DOCS',                                                                  NULL, 14)
ON CONFLICT (group_code, code) DO NOTHING;

-- ContainerType (code=getCode(), label=description)
-- EnumRegistryFactory: new EnumOption(e.name(), e.getCode(), e.getDescription())
-- code = enum.name() (SSOT), label = getCode() (짧은 표시용), labelKo = description
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('ContainerType', 'T20GP', '20GP', '20` 8` General purpose container',                    0),
    ('ContainerType', 'T20FR', '20FR', '20'' 8'' Flat-rack container',                        1),
    ('ContainerType', 'T20OT', '20OT', '20'' 8'' Open-top container',                         2),
    ('ContainerType', 'T20RF', '20RF', '20'' 8'' Reefer container',                           3),
    ('ContainerType', 'T20TC', '20TC', '20'' 8'' TANK CONTAINER',                             4),
    ('ContainerType', 'T20HT', '20HT', '20'' 8'' Hanger container',                          5),
    ('ContainerType', 'T20GH', '20GH', '20'' GARMENT ON HANGER',                             6),
    ('ContainerType', 'T20RH', '20RH', '20'' REEFER',                                        7),
    ('ContainerType', 'T20HZ', '20HZ', '20'' HZ Container',                                  8),
    ('ContainerType', 'T20HQ', '20HQ', '20''8'' HIGH-QUBIC CONTAINER',                       9),
    ('ContainerType', 'T22GP', '22GP', '20` 8` General purpose container',                   10),
    ('ContainerType', 'T22RE', '22RE', '20` 8` Termal container',                            11),
    ('ContainerType', 'T22UT', '22UT', '20` 8` Open-top container',                          12),
    ('ContainerType', 'T22PL', '22PL', '20` 8` Platform Container',                          13),
    ('ContainerType', 'F40GP', '40GP', '40'' 8'' General purpose container',                 14),
    ('ContainerType', 'F40FR', '40FR', '40'' 8'' Flat-rack container',                       15),
    ('ContainerType', 'F40OT', '40OT', '40'' 8'' Open-top container',                        16),
    ('ContainerType', 'F40RF', '40RF', '40'' 8'' Reefer container',                          17),
    ('ContainerType', 'F40TC', '40TC', '40'' 8'' TANK CONTAINER',                            18),
    ('ContainerType', 'F40HT', '40HT', '40'' 8'' Hanger container',                          19),
    ('ContainerType', 'F40GH', '40GH', '40'' GARMENT ON HANGER',                             20),
    ('ContainerType', 'F40RH', '40RH', '40'' 8'' Reefer container',                          21),
    ('ContainerType', 'F40HQ', '40HQ', '40` 8` HIGH-QUBIC container',                        22),
    ('ContainerType', 'F40SR', '40SR', '40'' SUPER RACK',                                    23),
    ('ContainerType', 'F40HS', '40HS', '40''HC SUPER RACK CONTAINER',                        24),
    ('ContainerType', 'F40NR', '40NR', '40` Non-Operational Refrigerated Containers',         25),
    ('ContainerType', 'F40FH', '40FH', '40''HC FLAT RACK CONTAINER',                         26),
    ('ContainerType', 'F42GP', '42GP', '40` 8` General purpose container',                   27),
    ('ContainerType', 'F42RE', '42RE', '40` 8` Termal container',                            28),
    ('ContainerType', 'F42UT', '42UT', '40` 8` Open-top container',                          29),
    ('ContainerType', 'F42PL', '42PL', '40` 8` Platform Container',                          30),
    ('ContainerType', 'F45GP', '45GP', '40` 8` dry container',                               31),
    ('ContainerType', 'F45FR', '45FR', '45 FLAT-RACK CONTAINER',                             32),
    ('ContainerType', 'F45RE', '45RE', '40'' 9'' 6" THERMAL CONTAINER',                      33),
    ('ContainerType', 'F45HQ', '45HQ', '40` 9` HIGH-QUBIC container',                        34),
    ('ContainerType', 'F45R1', '45R1', '40HQ REFRIGERATED',                                  35)
ON CONFLICT (group_code, code) DO NOTHING;

-- TruckType (code=name(), label=label)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('TruckType', 'T12',  'T12',  NULL, 0),
    ('TruckType', 'T25',  'T25',  NULL, 1),
    ('TruckType', 'T35',  'T35',  NULL, 2),
    ('TruckType', 'T50',  'T50',  NULL, 3),
    ('TruckType', 'T80',  'T80',  NULL, 4),
    ('TruckType', 'T100', 'T100', NULL, 5)
ON CONFLICT (group_code, code) DO NOTHING;

-- SalesClass (code=name(), label=label)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('SalesClass', 'S', 'Sales', NULL, 0),
    ('SalesClass', 'N', 'Nomi',  NULL, 1)
ON CONFLICT (group_code, code) DO NOTHING;

-- NoOfBl (code=name(), label=label)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('NoOfBl', 'ZERO',  'ZERO(0)',  NULL, 0),
    ('NoOfBl', 'ONE',   'ONE(1)',   NULL, 1),
    ('NoOfBl', 'TWO',   'TWO(2)',   NULL, 2),
    ('NoOfBl', 'THREE', 'THREE(3)', NULL, 3)
ON CONFLICT (group_code, code) DO NOTHING;

-- housebl.JobDiv (code=name(), label=label, labelKo=labelKo)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('housebl.JobDiv', 'SEA',    'Sea',     '해상', 0),
    ('housebl.JobDiv', 'AIR',    'Air',     '항공', 1),
    ('housebl.JobDiv', 'TRUCK',  'Truck',   '육로', 2),
    ('housebl.JobDiv', 'NON_BL', 'Non B/L', NULL,   3)
ON CONFLICT (group_code, code) DO NOTHING;

-- masterbl.MasterBlJobDiv (code=name(), label=label, labelKo=labelKo)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('masterbl.MasterBlJobDiv', 'SEA', 'Sea', '해상', 0),
    ('masterbl.MasterBlJobDiv', 'AIR', 'Air', '항공', 1)
ON CONFLICT (group_code, code) DO NOTHING;

-- TaxType (code=name(), label=label, labelKo=labelKo)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('TaxType', 'TAXABLE',    'Taxable',    '과세', 0),
    ('TaxType', 'ZERO_RATED', 'Zero-rated', '영세', 1),
    ('TaxType', 'EXEMPT',     'Exempt',     '면세', 2)
ON CONFLICT (group_code, code) DO NOTHING;

-- FinancialDocType (code=name(), label=name() — EnumRegistryFactory: e.name(), e.name())
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('FinancialDocType', 'INVOICE', 'INVOICE', NULL, 0),
    ('FinancialDocType', 'PAYMENT', 'PAYMENT', NULL, 1),
    ('FinancialDocType', 'DEBIT',   'DEBIT',   NULL, 2),
    ('FinancialDocType', 'CREDIT',  'CREDIT',  NULL, 3)
ON CONFLICT (group_code, code) DO NOTHING;

-- DocumentStatus (BMS origin, PMS 복제 공유)
-- BMS: name()만 보유. PMS는 label/labelKo 보유 → PMS 값이 더 풍부하므로 PMS label 채용.
-- PMS: CREATED=Created/생성, GROUPED=Grouped/그룹화, TAX=Tax Issued/세금계산서 발행,
--       SLIP=Slip Issued/전표 발행, CLEAR=Cleared/완료
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('DocumentStatus', 'CREATED', 'Created',      '생성',         0),
    ('DocumentStatus', 'GROUPED', 'Grouped',       '그룹화',       1),
    ('DocumentStatus', 'TAX',     'Tax Issued',    '세금계산서 발행', 2),
    ('DocumentStatus', 'SLIP',    'Slip Issued',   '전표 발행',    3),
    ('DocumentStatus', 'CLEAR',   'Cleared',       '완료',         4)
ON CONFLICT (group_code, code) DO NOTHING;

-- DocumentType (BMS origin, PMS 복제 공유)
-- BMS: initial() 보유, PMS: label/labelKo 보유 → PMS label 채용.
-- PMS: INVOICE=Invoice/청구서, PAYMENT=Payment/수금, DEBIT=Debit/차변, CREDIT=Credit/대변
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('DocumentType', 'INVOICE', 'Invoice', '청구서', 0),
    ('DocumentType', 'PAYMENT', 'Payment', '수금',   1),
    ('DocumentType', 'DEBIT',   'Debit',   '차변',   2),
    ('DocumentType', 'CREDIT',  'Credit',  '대변',   3)
ON CONFLICT (group_code, code) DO NOTHING;

-- GroupCategory BMS (code=name(), label=name())
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('GroupCategory', 'INVOICE', 'INVOICE', NULL, 0),
    ('GroupCategory', 'PAYMENT', 'PAYMENT', NULL, 1),
    ('GroupCategory', 'DCNOTE',  'DCNOTE',  NULL, 2)
ON CONFLICT (group_code, code) DO NOTHING;

-- IssueType BMS (code=name(), label=name())
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('IssueType', 'TAX',  'TAX',  NULL, 0),
    ('IssueType', 'SLIP', 'SLIP', NULL, 1)
ON CONFLICT (group_code, code) DO NOTHING;

-- AggregationBasis PMS (code=name(), label=label, labelKo=labelKo)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('AggregationBasis', 'FREIGHT_INPUT',    'Freight Input',        '운임 입력 건',     0),
    ('AggregationBasis', 'DOCUMENT_CREATED', 'Document Created',     '서류 생성',       1),
    ('AggregationBasis', 'TAX_ISSUED',       'Tax Invoice Issued',   '세금계산서 발행', 2),
    ('AggregationBasis', 'SLIP_ISSUED',      'Slip Issued',          '전표 발행',       3)
ON CONFLICT (group_code, code) DO NOTHING;

-- DateKind PMS (code=name(), label=label, labelKo=labelKo)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('DateKind', 'ETD',  'ETD',        'ETD',   0),
    ('DateKind', 'ETA',  'ETA',        'ETA',   1),
    ('DateKind', 'PERF', 'Perf. Date', '실적일자', 2),
    ('DateKind', 'DOC',  'Doc. Date',  '서류일자', 3)
ON CONFLICT (group_code, code) DO NOTHING;

-- PortKind PMS (code=name(), label=label, labelKo=labelKo)
INSERT INTO admin.common_code (group_code, code, label, label_ko, sort_order) VALUES
    ('PortKind', 'POL', 'POL', 'POL', 0),
    ('PortKind', 'POD', 'POD', 'POD', 1)
ON CONFLICT (group_code, code) DO NOTHING;
