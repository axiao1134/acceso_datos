import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Postsql {
    // Constantes para la conexión a PostgreSQL
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "milla crea la misma base dadatso e ingrese tu contraseña"; // Reemplaza con tu contraseña

    // Expresión regular para el formato de fecha YYYY-MM-DD
    private static final String DATE_REGEX = "\\b\\d{4}-\\d{2}-\\d{2}\\b";

    // Método para establecer la conexión con PostgreSQL
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    // Método para mostrar el menú de operaciones disponibles
    private static void mostrarMenuOperaciones() {
        System.out.println("=== Menú de Operaciones ===");
        System.out.println("Seleccione la operación:");
        System.out.println("1. Ver");
        System.out.println("2. Insertar");
        System.out.println("3. Actualizar");
        System.out.println("4. Eliminar");
        System.out.println("5. Salir");
        System.out.print("Seleccione una opción: ");
    }

    // Método para el menú de Ver
    private static void menuVer(Scanner scanner) {
        System.out.println("=== Menú Ver ===");
        System.out.println("Seleccione la tabla:");
        System.out.println("1. Autor");
        System.out.println("2. Libro");
        System.out.print("Seleccione una tabla: ");

        int opcionTabla = scanner.nextInt();
        scanner.nextLine(); // Consumir el salto de línea pendiente

        switch (opcionTabla) {
            case 1:
                verAutores();
                break;
            case 2:
                verLibros();
                break;
            default:
                System.out.println("Opción inválida.");
        }
    }

    // Método para ver los autores
    private static void verAutores() {
        String sql = "SELECT * FROM Autor";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("=== Autores ===");
            while (rs.next()) {
                String nombre = rs.getString("nombreApellidoautor");
                String fechaNacimiento = rs.getString("FechaNacimiento");
                String nacionalidad = rs.getString("Nacionalidad");

                System.out.println("Nombre: " + nombre + ", Fecha de Nacimiento: " + fechaNacimiento + ", Nacionalidad: " + nacionalidad);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para ver los libros
    private static void verLibros() {
        String sql = "SELECT * FROM Libro";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("=== Libros ===");
            while (rs.next()) {
                int idLibro = rs.getInt("IDlibro");
                String titulo = rs.getString("Titulo");
                int anioPublicacion = rs.getInt("anioPublicacion");
                String nombreAutor = rs.getString("nombreApellidoautor");
                String editorial = rs.getString("Editorial");

                System.out.println("ID: " + idLibro + ", Título: " + titulo + ", Año de Publicación: " + anioPublicacion + ", Autor: " + nombreAutor + ", Editorial: " + editorial);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para insertar un autor
    private static void insertarAutor(Scanner scanner) {
        System.out.println("Agregar Nuevo Autor:");

        System.out.print("Nombre y Apellido: ");
        String nombreAutor = scanner.nextLine();

        // Validar formato de fecha de nacimiento
        String fechaNacimiento;
        while (true) {
            System.out.print("Fecha de Nacimiento (YYYY-MM-DD): ");
            fechaNacimiento = scanner.nextLine();

            if (isValidDateFormat(fechaNacimiento)) {
                break;
            } else {
                System.out.println("Formato de fecha incorrecto. Por favor, ingrese la fecha en formato YYYY-MM-DD.");
            }
        }

        System.out.print("Nacionalidad: ");
        String nacionalidad = scanner.nextLine();

        String sql = "INSERT INTO Autor (nombreApellidoautor, FechaNacimiento, Nacionalidad) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreAutor);
            stmt.setDate(2, java.sql.Date.valueOf(fechaNacimiento));
            stmt.setString(3, nacionalidad);

            int filasInsertadas = stmt.executeUpdate();
            if (filasInsertadas > 0) {
                System.out.println("Autor insertado correctamente.");
            } else {
                System.out.println("Error al insertar el autor.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para insertar un libro
    private static void insertarLibro(Scanner scanner) {
        System.out.println("Insertar Libro:");
        System.out.print("Título: ");
        String tituloLibro = scanner.nextLine();

        System.out.print("Año de Publicación: ");
        int anioPublicacion = scanner.nextInt();
        scanner.nextLine(); // Consumir el salto de línea pendiente

        System.out.print("Nombre del Autor: ");
        String autorLibro = scanner.nextLine();

        System.out.print("Editorial: ");
        String editorialLibro = scanner.nextLine();

        String sqlBuscarAutor = "SELECT COUNT(*) AS count FROM Autor WHERE nombreApellidoautor = ?";
        String sqlInsertarLibro = "INSERT INTO Libro (Titulo, anioPublicacion, nombreApellidoautor, Editorial) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmtBuscar = conn.prepareStatement(sqlBuscarAutor)) {

            // Verificar si el autor existe
            stmtBuscar.setString(1, autorLibro);
            ResultSet rs = stmtBuscar.executeQuery();
            rs.next();
            int rowCount = rs.getInt("count");

            if (rowCount == 0) {
                System.out.println("Autor no registrado. No se puede insertar el libro.");
                return;
            }

            // Insertar el libro
            PreparedStatement stmtInsertarLibro = conn.prepareStatement(sqlInsertarLibro);
            stmtInsertarLibro.setString(1, tituloLibro);
            stmtInsertarLibro.setInt(2, anioPublicacion);
            stmtInsertarLibro.setString(3, autorLibro);
            stmtInsertarLibro.setString(4, editorialLibro);

            int filasInsertadas = stmtInsertarLibro.executeUpdate();
            if (filasInsertadas > 0) {
                System.out.println("Libro insertado correctamente.");
            } else {
                System.out.println("Error al insertar el libro.");
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("violates foreign key constraint")) {
                System.out.println("Autor no registrado. No se puede insertar el libro.");
            } else {
                e.printStackTrace();
            }
        }
    }

    // Método para eliminar un autor por nombre
    private static void eliminarAutor(Scanner scanner) {
        System.out.println("Eliminar Autor:");
        System.out.print("Ingrese el Nombre y Apellido del Autor que desea eliminar: ");
        String nombreAutor = scanner.nextLine();

        String sqlEliminarAutor = "DELETE FROM Autor WHERE nombreApellidoautor = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlEliminarAutor)) {

            stmt.setString(1, nombreAutor);

            int filasEliminadas = stmt.executeUpdate();
            if (filasEliminadas > 0) {
                System.out.println("Autor eliminado correctamente.");
            } else {
                System.out.println("No se encontró ningún autor con ese nombre.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para eliminar un libro por ID
    private static void eliminarLibro(Scanner scanner) {
        System.out.println("Eliminar Libro:");
        System.out.print("Ingrese el ID del Libro que desea eliminar: ");
        int idLibro = scanner.nextInt();
        scanner.nextLine(); // Consumir el salto de línea pendiente

        String sqlBuscarLibro = "SELECT * FROM Libro WHERE IDlibro = ?";
        String sqlEliminarLibro = "DELETE FROM Libro WHERE IDlibro = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmtBuscar = conn.prepareStatement(sqlBuscarLibro);
             PreparedStatement stmtEliminar = conn.prepareStatement(sqlEliminarLibro)) {

            stmtBuscar.setInt(1, idLibro);
            ResultSet rs = stmtBuscar.executeQuery();

            if (!rs.next()) {
                System.out.println("No se encontró ningún libro con ese ID.");
                return;
            }

            stmtEliminar.setInt(1, idLibro);
            int filasEliminadas = stmtEliminar.executeUpdate();

            if (filasEliminadas > 0) {
                System.out.println("Libro eliminado correctamente.");
            } else {
                System.out.println("Error al eliminar el libro.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para actualizar un libro por ID
    private static void actualizarLibro(Scanner scanner) {
        System.out.println("Actualizar Libro:");

        // Solicitar ID del libro y verificar su existencia
        int idLibro;
        while (true) {
            System.out.print("Ingrese el ID del Libro que desea actualizar: ");
            idLibro = scanner.nextInt();
            scanner.nextLine(); // Consumir el salto de línea pendiente

            String sqlBuscarLibro = "SELECT * FROM Libro WHERE IDlibro = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmtBuscar = conn.prepareStatement(sqlBuscarLibro)) {

                stmtBuscar.setInt(1, idLibro);
                ResultSet rs = stmtBuscar.executeQuery();

                if (rs.next()) {
                    break; // ID válido encontrado, salir del bucle
                } else {
                    System.out.println("No se encontró ningún libro con ese ID. Intente nuevamente.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Mostrar las columnas disponibles para actualizar
        System.out.println("Seleccione la columna que desea actualizar:");
        System.out.println("1. Título");
        System.out.println("2. Año de Publicación");
        System.out.println("3. Nombre del Autor");
        System.out.println("4. Editorial");
        System.out.print("Seleccione una columna: ");

        int opcionColumna = scanner.nextInt();
        scanner.nextLine(); // Consumir el salto de línea pendiente

        String columna;
        String valorNuevo;
        switch (opcionColumna) {
            case 1:
                columna = "Titulo";
                System.out.print("Nuevo Título: ");
                valorNuevo = scanner.nextLine();
                break;
            case 2:
                columna = "anioPublicacion";
                System.out.print("Nuevo Año de Publicación: ");
                valorNuevo = scanner.nextLine();
                break;
            case 3:
                columna = "nombreApellidoautor";
                System.out.print("Nuevo Nombre del Autor: ");
                valorNuevo = scanner.nextLine();

                // Verificar si el nuevo autor está registrado
                String sqlBuscarAutor = "SELECT COUNT(*) AS count FROM Autor WHERE nombreApellidoautor = ?";
                try (Connection conn = getConnection();
                     PreparedStatement stmtBuscarAutor = conn.prepareStatement(sqlBuscarAutor)) {

                    stmtBuscarAutor.setString(1, valorNuevo);
                    ResultSet rs = stmtBuscarAutor.executeQuery();
                    rs.next();
                    int rowCount = rs.getInt("count");

                    if (rowCount == 0) {
                        System.out.println("Autor no registrado. No se puede actualizar el libro.");
                        return;
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 4:
                columna = "Editorial";
                System.out.print("Nueva Editorial: ");
                valorNuevo = scanner.nextLine();
                break;
            default:
                System.out.println("Opción inválida.");
                return;
        }

        String sqlActualizarLibro = "UPDATE Libro SET " + columna + " = ? WHERE IDlibro = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmtActualizar = conn.prepareStatement(sqlActualizarLibro)) {

            stmtActualizar.setString(1, valorNuevo);
            stmtActualizar.setInt(2, idLibro);

            int filasActualizadas = stmtActualizar.executeUpdate();
            if (filasActualizadas > 0) {
                System.out.println("Libro actualizado correctamente.");
            } else {
                System.out.println("No se encontró ningún libro con ese ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para validar el formato de fecha YYYY-MM-DD
    private static boolean isValidDateFormat(String dateStr) {
        Pattern pattern = Pattern.compile(DATE_REGEX);
        Matcher matcher = pattern.matcher(dateStr);
        return matcher.matches();
    }

    // Método principal (main) para ejecutar el programa
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            mostrarMenuOperaciones();
            int opcionOperacion = scanner.nextInt();
            scanner.nextLine(); // Consumir el salto de línea pendiente

            switch (opcionOperacion) {
                case 1:
                    menuVer(scanner);
                    break;
                case 2:
                    System.out.println("=== Menú Insertar ===");
                    System.out.println("Seleccione la tabla:");
                    System.out.println("1. Autor");
                    System.out.println("2. Libro");
                    System.out.print("Seleccione una tabla: ");

                    int opcionTablaInsertar = scanner.nextInt();
                    scanner.nextLine(); // Consumir el salto de línea pendiente

                    switch (opcionTablaInsertar) {
                        case 1:
                            insertarAutor(scanner);
                            break;
                        case 2:
                            insertarLibro(scanner);
                            break;
                        default:
                            System.out.println("Opción inválida.");
                    }
                    break;
                case 3:
                    System.out.println("=== Menú Actualizar ===");
                    System.out.println("Seleccione la tabla:");
                    System.out.println("1. Autor");
                    System.out.println("2. Libro");
                    System.out.print("Seleccione una tabla: ");

                    int opcionTablaActualizar = scanner.nextInt();
                    scanner.nextLine(); // Consumir el salto de línea pendiente

                    switch (opcionTablaActualizar) {
                        case 1:
                            System.out.println("Actualizar Autor: Función no disponible.");
                            break;
                        case 2:
                            actualizarLibro(scanner);
                            break;
                        default:
                            System.out.println("Opción inválida.");
                    }
                    break;
                case 4:
                    System.out.println("=== Menú Eliminar ===");
                    System.out.println("Seleccione la tabla:");
                    System.out.println("1. Autor");
                    System.out.println("2. Libro");
                    System.out.print("Seleccione una tabla: ");

                    int opcionTablaEliminar = scanner.nextInt();
                    scanner.nextLine(); // Consumir el salto de línea pendiente

                    switch (opcionTablaEliminar) {
                        case 1:
                            eliminarAutor(scanner);
                            break;
                        case 2:
                            eliminarLibro(scanner);
                            break;
                        default:
                            System.out.println("Opción inválida.");
                    }
                    break;
                case 5:
                    System.out.println("Saliendo del programa.");
                    System.exit(0);
                default:
                    System.out.println("Opción inválida. Intente nuevamente.");
            }

            System.out.println(); // Agregar línea en blanco entre iteraciones del menú
        }
    }
}
// profe estoy cansao
