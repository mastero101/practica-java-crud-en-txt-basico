import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegistroUsuario extends JFrame {
    private JTextField txtNombre;
    private JPasswordField txtContraseña;
    private JTextField txtCorreo;
    private DefaultTableModel tableModel;
    private Connection conexion;

    public RegistroUsuario() {
        // Configuración del frame
        setTitle("Registro de Usuario");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear panel y layout
        JPanel panel = new JPanel(new BorderLayout());

        // Componentes del formulario de registro
        JPanel registroPanel = new JPanel();
        registroPanel.setLayout(new GridLayout(4, 2));

        JLabel lblNombre = new JLabel("Nombre:");
        txtNombre = new JTextField();
        JLabel lblContraseña = new JLabel("Contraseña:");
        txtContraseña = new JPasswordField();
        JLabel lblCorreo = new JLabel("Correo:");
        txtCorreo = new JTextField();
        JButton btnRegistrar = new JButton("Registrar");

        registroPanel.add(lblNombre);
        registroPanel.add(txtNombre);
        registroPanel.add(lblContraseña);
        registroPanel.add(txtContraseña);
        registroPanel.add(lblCorreo);
        registroPanel.add(txtCorreo);
        registroPanel.add(new JLabel()); // Espacio en blanco
        registroPanel.add(btnRegistrar);

        // Tabla para mostrar y editar los usuarios registrados
        JTable tablaUsuarios = new JTable();
        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre", "Contraseña", "Correo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Permitir la edición en todas las columnas excepto la primera (ID)
            }
        };
        tablaUsuarios.setModel(tableModel);

        // Panel para el botón "Eliminar"
        JPanel panelEliminar = new JPanel();
        panelEliminar.setLayout(new FlowLayout(FlowLayout.RIGHT)); // Alineación a la derecha

        JButton btnEliminar = new JButton("Eliminar");
        panelEliminar.add(btnEliminar);

        // Agregar componentes al panel
        panel.add(registroPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);
        panel.add(panelEliminar, BorderLayout.SOUTH);

        // Acción del botón Registrar
        btnRegistrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registrarUsuario();
            }
        });

        // Acción del botón Eliminar
        btnEliminar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eliminarUsuario(tablaUsuarios);
            }
        });

        // Conectar a la base de datos MySQL
        conectarBD();

        // Cargar usuarios registrados al iniciar la aplicación
        cargarUsuarios();

        // Agregar panel al frame
        add(panel);

        // Mostrar el frame
        setVisible(true);
    }

    private void conectarBD() {
        try {
            String url = "jdbc:mysql://localhost:3306/java_prueba";
            String usuario = "mastero";
            String contraseña = "alejandrof15";
            conexion = DriverManager.getConnection(url, usuario, contraseña);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al conectar a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarUsuario() {
        String nombre = txtNombre.getText();
        String contraseña = new String(txtContraseña.getPassword());
        String correo = txtCorreo.getText();

        try {
            String sql = "INSERT INTO usuarios (nombre, contraseña, correo) VALUES (?, ?, ?)";
            PreparedStatement statement = conexion.prepareStatement(sql);
            statement.setString(1, nombre);
            statement.setString(2, contraseña);
            statement.setString(3, correo);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Usuario registrado correctamente.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al registrar usuario.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Mostrar usuario registrado en la tabla
        cargarUsuarios();
        limpiarCampos();
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtContraseña.setText("");
        txtCorreo.setText("");
    }

    private void cargarUsuarios() {
        tableModel.setRowCount(0); // Limpiar la tabla antes de cargar los usuarios
        try {
            Statement statement = conexion.createStatement();
            ResultSet resultado = statement.executeQuery("SELECT * FROM usuarios");
            while (resultado.next()) {
                int id = resultado.getInt("id");
                String nombre = resultado.getString("nombre");
                String contraseña = resultado.getString("contraseña");
                String correo = resultado.getString("correo");
                tableModel.addRow(new Object[]{id, nombre, contraseña, correo});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void eliminarUsuario(JTable tablaUsuarios) {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada != -1) {
            int idUsuario = (int) tablaUsuarios.getValueAt(filaSeleccionada, 0);
            try {
                String sql = "DELETE FROM usuarios WHERE id = ?";
                PreparedStatement statement = conexion.prepareStatement(sql);
                statement.setInt(1, idUsuario);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Usuario eliminado correctamente.");
                cargarUsuarios();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al eliminar usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new RegistroUsuario();
            }
        });
    }
}

