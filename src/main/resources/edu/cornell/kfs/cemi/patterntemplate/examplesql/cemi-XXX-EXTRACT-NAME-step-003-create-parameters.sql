-- At a minimum this "create-parameters" script file needs to define the two system parameters listed below.
--    Replace {EXTRACTNAME} in both parameter definitions below for the data extraction being created.
INSERT INTO KFS.KRCR_PARM_T (
    NMSPC_CD,
    CMPNT_CD,
    PARM_NM,
    OBJ_ID,
    VER_NBR,
    PARM_TYP_CD,
    VAL,
    PARM_DESC_TXT,
    EVAL_OPRTR_CD
)
VALUES (
    'KFS-CEMI',
    'CreateCemi{EXTRACTNAME}ExtractStep',
    'COPY_CEMI_{EXTRACT_NAME}_FILE_TO_OUTBOUND_FOLDER',
    SYS_GUID(),
    1,
    'CONFG',
    'N',
    'Denotes whether the generated CEMI {EXTRACT NAME} Extract file should be copied to the outbound folder, '
        || 'so that it can be forwarded to Huron for further processing.',
    'A'
);

INSERT INTO KFS.KRCR_PARM_T (
    NMSPC_CD,
    CMPNT_CD,
    PARM_NM,
    OBJ_ID,
    VER_NBR,
    PARM_TYP_CD,
    VAL,
    PARM_DESC_TXT,
    EVAL_OPRTR_CD
) VALUES (
    'KFS-CEMI',
    'CreateCemi{EXTRACTNAME}ExtractStep',
    'CEMI_SENSITIVE_DATA_MASKING_SETTING',
    SYS_GUID(),
    1,
    'CONFG',
    'MASK',
    'Denotes whether the generated CEMI {EXTRACT NAME} Extract file should be in MASK or UNMASK state.',
    'A'
);