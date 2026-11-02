import java.sql.*;

// 1. CLASSE DE CONEXÃO E PERSISTÊNCIA
class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:metrologia_schulz.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void inicializarBanco(String ddlScript) {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : ddlScript.split(";")) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar banco: " + e.getMessage());
        }
    }
}

// 2. CLASSES DE DOMÍNIO (POO)
class ItemCalibrado {
    private int idItem;
    private String cdInstrumento;
    private String nmDenominacao;
    private String nmFabricante;
    private double vlrNominalGeral;

    public ItemCalibrado(String cdInstrumento, String nmDenominacao, String nmFabricante, double vlrNominalGeral) {
        this.cdInstrumento = cdInstrumento;
        this.nmDenominacao = nmDenominacao;
        this.nmFabricante = nmFabricante;
        this.vlrNominalGeral = vlrNominalGeral;
    }

    public int getIdItem() { return idItem; }
    public String getCdInstrumento() { return cdInstrumento; }
    public String getNmDenominacao() { return nmDenominacao; }

    public void salvar() throws SQLException {
        String sql = "INSERT INTO tb_item_calibrado (cd_instrumento, nm_denominacao, nm_fabricante, vlr_nominal_geral) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, this.cdInstrumento);
            pstmt.setString(2, this.nmDenominacao);
            pstmt.setString(3, this.nmFabricante);
            pstmt.setDouble(4, this.vlrNominalGeral);
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) this.idItem = generatedKeys.getInt(1);
            }
        }
    }
}

class CertificadoCalibracao {
    private int idCertificado;
    private String nrCertificado;
    private String nrOs;
    private String dtCalibracao;
    private String dtValidade;
    private String nmTecnico;
    private String nmSolicitante;
    private double vlrTemperaturaAmbiente;
    private String dsObservacoes;

    public CertificadoCalibracao(String nrCertificado, String nrOs, String dtCalibracao, String dtValidade, String nmTecnico, String nmSolicitante, double vlrTemperaturaAmbiente, String dsObservacoes) {
        this.nrCertificado = nrCertificado;
        this.nrOs = nrOs;
        this.dtCalibracao = dtCalibracao;
        this.dtValidade = dtValidade;
        this.nmTecnico = nmTecnico;
        this.nmSolicitante = nmSolicitante;
        this.vlrTemperaturaAmbiente = vlrTemperaturaAmbiente;
        this.dsObservacoes = dsObservacoes;
    }

    public int getIdCertificado() { return idCertificado; }
    public String getNrCertificado() { return nrCertificado; }
    public String getNmTecnico() { return nmTecnico; }

    public void salvar() throws SQLException {
        String sql = "INSERT INTO tb_certificado_calibracao (nr_certificado, nr_os, dt_calibracao, dt_validade, nm_tecnico, nm_solicitante, vlr_temperatura_ambiente, ds_observacoes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, this.nrCertificado);
            pstmt.setString(2, this.nrOs);
            pstmt.setString(3, this.dtCalibracao);
            pstmt.setString(4, this.dtValidade);
            pstmt.setString(5, this.nmTecnico);
            pstmt.setString(6, this.nmSolicitante);
            pstmt.setDouble(7, this.vlrTemperaturaAmbiente);
            pstmt.setString(8, this.dsObservacoes);
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) this.idCertificado = generatedKeys.getInt(1);
            }
        }
    }
}

class ResultadoMedicao {
    private int idResultado;
    private int idCertificado;
    private int idItem;
    private String nmParametro;
    private double vlrNominal;
    private double tolSuperior;
    private double tolInferior;
    private double vlrMedida1;
    private double vlrMedida2;
    private double vlrMedia;
    private double vlrDesvioPadrao;
    private double vlrErro;
    private double vlrIncertezaExp;
    private double vlrFatorK;
    private String strStatusAvaliacao;

    public ResultadoMedicao(int idCertificado, int idItem, String nmParametro, double vlrNominal, double tolSuperior, double tolInferior, double vlrMedida1, double vlrMedida2, double vlrIncertezaExp, double vlrFatorK) {
        this.idCertificado = idCertificado;
        this.idItem = idItem;
        this.nmParametro = nmParametro;
        this.vlrNominal = vlrNominal;
        this.tolSuperior = tolSuperior;
        this.tolInferior = tolInferior;
        this.vlrMedida1 = vlrMedida1;
        this.vlrMedida2 = vlrMedida2;
        this.vlrIncertezaExp = vlrIncertezaExp;
        this.vlrFatorK = vlrFatorK;

        calcularLogica();
    }

    private void calcularLogica() {
        this.vlrMedia = (this.vlrMedida1 + this.vlrMedida2) / 2.0;
        this.vlrDesvioPadrao = Math.abs(this.vlrMedida1 - this.vlrMedia);
        this.vlrErro = this.vlrMedia - this.vlrNominal;

        if (this.vlrMedia >= this.tolInferior && this.vlrMedia <= this.tolSuperior) {
            this.strStatusAvaliacao = "APROVADO";
        } else {
            this.strStatusAvaliacao = "REPROVADO";
        }
    }

    public void salvar() throws SQLException {
        String sql = "INSERT INTO tb_resultado_medicao (id_certificado, id_item, nm_parametro, vlr_nominal, tol_superior, tol_inferior, vlr_medida_1, vlr_medida_2, vlr_media, vlr_desvio_padrao, vlr_erro, vlr_incerteza_exp, vlr_fator_k, str_status_avaliacao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.idCertificado);
            pstmt.setInt(2, this.idItem);
            pstmt.setString(3, this.nmParametro);
            pstmt.setDouble(4, this.vlrNominal);
            pstmt.setDouble(5, this.tolSuperior);
            pstmt.setDouble(6, this.tolInferior);
            pstmt.setDouble(7, this.vlrMedida1);
            pstmt.setDouble(8, this.vlrMedida2);
            pstmt.setDouble(9, this.vlrMedia);
            pstmt.setDouble(10, this.vlrDesvioPadrao);
            pstmt.setDouble(11, this.vlrErro);
            pstmt.setDouble(12, this.vlrIncertezaExp);
            pstmt.setDouble(13, this.vlrFatorK);
            pstmt.setString(14, this.strStatusAvaliacao);
            pstmt.executeUpdate();
        }
    }
}
