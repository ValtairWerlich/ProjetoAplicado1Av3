import java.sql.*;

public class Main {
    public static void main(String[] args) {
        String ddlScript = "PRAGMA foreign_keys = ON;\n" +
                "CREATE TABLE IF NOT EXISTS tb_item_calibrado (\n" +
                "    id_item INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    cd_instrumento TEXT NOT NULL,\n" +
                "    nm_denominacao TEXT NOT NULL,\n" +
                "    nm_fabricante TEXT,\n" +
                "    vlr_nominal_geral REAL\n" +
                ");\n" +
                "CREATE TABLE IF NOT EXISTS tb_certificado_calibracao (\n" +
                "    id_certificado INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    nr_certificado TEXT NOT NULL UNIQUE,\n" +
                "    nr_os TEXT NOT NULL,\n" +
                "    dt_calibracao TEXT NOT NULL,\n" +
                "    dt_validade TEXT,\n" +
                "    nm_tecnico TEXT NOT NULL,\n" +
                "    nm_solicitante TEXT NOT NULL,\n" +
                "    vlr_temperatura_ambiente REAL,\n" +
                "    ds_observacoes TEXT\n" +
                ");\n" +
                "CREATE TABLE IF NOT EXISTS tb_resultado_medicao (\n" +
                "    id_resultado INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    id_certificado INTEGER NOT NULL,\n" +
                "    id_item INTEGER NOT NULL,\n" +
                "    nm_parametro TEXT NOT NULL,\n" +
                "    vlr_nominal REAL NOT NULL,\n" +
                "    tol_superior REAL NOT NULL,\n" +
                "    tol_inferior REAL NOT NULL,\n" +
                "    vlr_medida_1 REAL NOT NULL,\n" +
                "    vlr_medida_2 REAL,\n" +
                "    vlr_media REAL NOT NULL,\n" +
                "    vlr_desvio_padrao REAL NOT NULL,\n" +
                "    vlr_erro REAL NOT NULL,\n" +
                "    vlr_incerteza_exp REAL NOT NULL,\n" +
                "    vlr_fator_k REAL NOT NULL,\n" +
                "    str_status_avaliacao TEXT NOT NULL,\n" +
                "    FOREIGN KEY (id_certificado) REFERENCES tb_certificado_calibracao (id_certificado) ON DELETE CASCADE ON UPDATE CASCADE,\n" +
                "    FOREIGN KEY (id_item) REFERENCES tb_item_calibrado (id_item) ON DELETE RESTRICT ON UPDATE CASCADE\n" +
                ");";

        System.out.println("=== Schulz S.A. Metrologia - Inicializando MVP ===");
        DatabaseConnection.inicializarBanco(ddlScript);

        try {
            ItemCalibrado micrometer = new ItemCalibrado("TAG-MIC-01", "Micrômetro Externo", "Mitutoyo", 25.0);
            micrometer.salvar();

            CertificadoCalibracao cert = new CertificadoCalibracao("CERT-2026-001", "OS-9982", "2026-11-02", "2027-11-02", "Hugo Menezes", "Qualidade Interna", 21.5, "Calibração padrão em laboratório climatizado.");
            cert.salvar();

            ResultadoMedicao med1 = new ResultadoMedicao(cert.getIdCertificado(), micrometer.getIdItem(), "Espessura", 25.0, 25.02, 24.98, 25.01, 24.99, 0.002, 2.0);
            med1.salvar();

            System.out.println("Dados de demonstração inseridos com sucesso!\n");
            exibirRelatorioCalibracao();

        } catch (SQLException e) {
            System.err.println("Erro na execução do fluxo funcional: " + e.getMessage());
        }
    }

    private static void exibirRelatorioCalibracao() {
        String queryJoin = "SELECT c.nr_certificado, c.nm_tecnico, i.cd_instrumento, i.nm_denominacao, r.vlr_media, r.str_status_avaliacao " +
                "FROM tb_resultado_medicao r " +
                "JOIN tb_certificado_calibracao c ON r.id_certificado = c.id_certificado " +
                "JOIN tb_item_calibrado i ON r.id_item = i.id_item";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryJoin)) {

            System.out.println("==========================================================================");
            System.out.println("                      RELATÓRIO DE CALIBRAÇÃO - MVP                        ");
            System.out.println("==========================================================================");
            System.out.printf("%-15s | %-15s | %-12s | %-10s | %-10s\n", "CERTIFICADO", "TÉCNICO", "TAG EQUIP.", "MÉDIA", "STATUS");
            System.out.println("--------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-15s | %-15s | %-12s | %-10.3f | %-10s\n",
                        rs.getString("nr_certificado"),
                        rs.getString("nm_tecnico"),
                        rs.getString("cd_instrumento"),
                        rs.getDouble("vlr_media"),
                        rs.getString("str_status_avaliacao"));
            }
            System.out.println("==========================================================================");

        } catch (SQLException e) {
            System.err.println("Erro ao gerar relatório: " + e.getMessage());
        }
    }
}
