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
    private JTable tablaUsuarios;
    private JPanel panelEliminar;

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
        registroPanel.add(new JLabel());
        registroPanel.add(btnRegistrar);

        // Tabla para mostrar y editar los usuarios registrados
        tablaUsuarios = new JTable();
        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre", "Contraseña", "Correo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Permitir la edición en todas las columnas excepto la primera (ID)
            }
        };
        tablaUsuarios.setModel(tableModel);

        // Panel para el botón "Eliminar"
        panelEliminar = new JPanel();
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
                eliminarUsuariosSeleccionados(tablaUsuarios);
            }
        });

        // Conectar a la base de datos MySQL
        conectarBD();

        // Cargar usuarios registrados al iniciar la aplicación
        cargarUsuarios();

        // Agregar panel al frame
        add(panel);

        BotonEditar();
        KeyListener();

        // Mostrar el frame
        setVisible(true);
    }

    private void conectarBD() {
        try {
            String url = "jdbc:mysql://192.168.1.89:3306/java_prueba";
            String usuario = "root";
            String contraseña = "123456";
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

    private void eliminarUsuariosSeleccionados(JTable tablaUsuarios) {
        int[] filasSeleccionadas = tablaUsuarios.getSelectedRows();
        if (filasSeleccionadas.length > 0) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar los usuarios seleccionados?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                try {
                    PreparedStatement statement = conexion.prepareStatement("DELETE FROM usuarios WHERE id = ?");
                    for (int fila : filasSeleccionadas) {
                        int idUsuario = (int) tablaUsuarios.getValueAt(fila, 0);
                        statement.setInt(1, idUsuario);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                    JOptionPane.showMessageDialog(this, "Usuarios eliminados correctamente.");
                    cargarUsuarios();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al eliminar usuarios.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione al menos un usuario para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }
    

    private void BotonEditar() {
        JButton btnEditar = new JButton("Editar");
        panelEliminar.add(btnEditar);
    
        // Acción del botón Editar
        btnEditar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaUsuarios.getSelectedRow();
                editarUsuario(filaSeleccionada);
            }
        });
    }
    
    private void editarUsuario(int filaSeleccionada) {
        if (filaSeleccionada != -1) {
            String nombre = (String) tablaUsuarios.getValueAt(filaSeleccionada, 1);
            String contraseña = (String) tablaUsuarios.getValueAt(filaSeleccionada, 2);
            String correo = (String) tablaUsuarios.getValueAt(filaSeleccionada, 3);
            int idUsuario = (int) tablaUsuarios.getValueAt(filaSeleccionada, 0);
            try {
                String sql = "UPDATE usuarios SET nombre = ?, contraseña = ?, correo = ? WHERE id = ?";
                PreparedStatement statement = conexion.prepareStatement(sql);
                statement.setString(1, nombre);
                statement.setString(2, contraseña);
                statement.setString(3, correo);
                statement.setInt(4, idUsuario);
                statement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Usuario actualizado correctamente.");
                cargarUsuarios(); // Recargar usuarios después de la actualización
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void KeyListener() {
        tablaUsuarios.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int filaSeleccionada = tablaUsuarios.getSelectedRow();
                    if (filaSeleccionada != -1) {
                        editarUsuario(filaSeleccionada);
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new RegistroUsuario();
            }
        });
    }
}

