import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class RegistroUsuario extends JFrame {
    private JTextField txtNombre;
    private JPasswordField txtContraseña;
    private JTextField txtCorreo;
    private DefaultTableModel tableModel;

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

        // Tabla para mostrar los usuarios registrados
        JTable tablaUsuarios = new JTable();
        tableModel = new DefaultTableModel(new Object[]{"Nombre", "Contraseña", "Correo"}, 0);
        tablaUsuarios.setModel(tableModel);

        // Agregar componentes al panel
        panel.add(registroPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);

        // Acción del botón Registrar
        btnRegistrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registrarUsuario();
            }
        });

        // Cargar usuarios registrados al iniciar la aplicación
        cargarUsuarios();

        // Agregar panel al frame
        add(panel);

        // Mostrar el frame
        setVisible(true);
    }

    private void registrarUsuario() {
        String nombre = txtNombre.getText();
        String contraseña = new String(txtContraseña.getPassword());
        String correo = txtCorreo.getText();

        // Guardar usuario en el archivo de texto
        try (FileWriter writer = new FileWriter("usuarios.txt", true);
             BufferedWriter bw = new BufferedWriter(writer);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(nombre + "," + contraseña + "," + correo);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Mostrar usuario registrado en la tabla
        tableModel.addRow(new Object[]{nombre, contraseña, correo});

        // Limpiar los campos
        limpiarCampos();
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtContraseña.setText("");
        txtCorreo.setText("");
    }

    private void cargarUsuarios() {
        File archivo = new File("usuarios.txt");
        if (!archivo.exists()) {
            return; // Si el archivo no existe, no hay usuarios que cargar
        }
    
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] partes = line.split(",");
                if (partes.length == 3) {
                    tableModel.addRow(partes);
                } else {
                    System.out.println("Error: formato de línea incorrecto en el archivo de usuarios.");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
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

