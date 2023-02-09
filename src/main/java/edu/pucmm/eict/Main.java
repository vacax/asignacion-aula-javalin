package edu.pucmm.eict;

import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.staticfiles.Location;
import static j2html.TagCreator.*;

/**
 * Ejemplo de asignación en aula.
 */
public class Main {

    public enum Constantes{
        USUARIO_LOGUEADO;
    }

    public static void main(String[] args) {
        System.out.println("Ejemplo de asignación en aula");
        /**
         * Configuración y arranque de Javalin.
         */
        var app = Javalin.create(javalinConfig -> {
                    javalinConfig.staticFiles.add(staticFileConfig -> {
                        staticFileConfig.hostedPath = "/";
                        staticFileConfig.directory = "/publico";
                        staticFileConfig.location = Location.CLASSPATH;
                    });
                })
                .start(7070);

        /**
         * Trabajando con el filtro, cualquier acceso sin el usuario auntenticado, debe enviarlo al recurso
         * login.html
         */
        app.before(ctx -> {
            //validando si existe el usuario logueado.
            System.out.println("Logueado: "+Constantes.USUARIO_LOGUEADO.name());
            Usuario usuario = ctx.sessionAttribute(Constantes.USUARIO_LOGUEADO.name());
            /**
             * Si, el usuario no está en la sesión, y la vista no es login.html y no es el endpoint de autenticar,
             * lo mando al login.html, lo contrario continuamos con la peticion.
             */
            if(usuario== null && !(ctx.path().contains("login.html") || ctx.path().contains("/autenticar"))){
                ctx.redirect("/login.html");
            }
        });

        /**
         * Endpoint visualizado una vez autenticado el usuario.
         */
        app.get("/", ctx -> {
            Usuario usuario = ctx.sessionAttribute(Constantes.USUARIO_LOGUEADO.name());
            // Probando el uso de la libreria j2html
            String texto = html(
                    head(
                            title("Asignacion en Aula")
                    ),
                    body(
                            h1("Bienvenido "+usuario.getUsername()),
                            p("Ejemplo de uso de la libreria j2html"),
                            a("Salir").withHref("/logout"))
            )
                    .render();
            //
            ctx.contentType(ContentType.TEXT_HTML);
            ctx.result(texto);
        });

        /**
         * Para nuestro ejemplo no importa los valores recibido, lo estaremos validando.
         */
        app.post("/autenticar", ctx -> {
            //
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            //
            Usuario usuario = new Usuario(username, password);
            ctx.sessionAttribute(Constantes.USUARIO_LOGUEADO.name(), usuario);
            //
            ctx.redirect("/");
        });

        /**
         * Proceso de salida de la sesión
         */
        app.get("/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.redirect("/");
        });
    }

    /**
     * Clase de encapsulación, puede ser un record
     */
    public static class Usuario{
        String username;
        String password;

        public Usuario() {
        }

        public Usuario(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}