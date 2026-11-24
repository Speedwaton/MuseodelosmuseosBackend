package cl.triskeledu.museo.tienda.servicio;

import cl.triskeledu.museo.tienda.dto.Dtos.ProductoRequest;
import cl.triskeledu.museo.tienda.error.ApiException;
import cl.triskeledu.museo.tienda.modelo.Producto;
import cl.triskeledu.museo.tienda.repositorio.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

// Reglas de negocio del catálogo.
@Service
public class ProductoService {
    private final ProductoRepository repositorio;

    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    // El Cliente ve solo los activos; el Administrador ve todos.
    public List<Producto> listar(boolean soloActivos) {
        return soloActivos ? repositorio.findByActivoTrueOrderByCategoriaAscNombreAsc() : repositorio.findAllByOrderByCategoriaAscNombreAsc();
    }

    public Producto obtener(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe el producto " + id + "."));
    }

    // Busca el producto de un código QR (por ejemplo PRD-006). Los desactivados no se muestran.
    public Producto porCodigo(String codigo) {
        return repositorio.findByCodigo(codigo.trim().toUpperCase()).filter(Producto::isActivo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El código " + codigo + " no corresponde a ningún producto disponible."));
    }

    public Producto crear(ProductoRequest datos) {
        validarCodigoLibre(datos.codigo(), null);
        Producto p = new Producto();
        copiar(datos, p);
        return repositorio.save(p);
    }

    public Producto actualizar(Long id, ProductoRequest datos) {
        Producto p = obtener(id);
        validarCodigoLibre(datos.codigo(), id);
        copiar(datos, p);
        return repositorio.save(p);
    }

    // Activa o desactiva un producto sin borrarlo.
    public Producto cambiarActivo(Long id, boolean activo) {
        Producto p = obtener(id);
        p.setActivo(activo);
        return repositorio.save(p);
    }

    private void validarCodigoLibre(String codigo, Long idActual) {
        repositorio.findByCodigo(codigo.trim().toUpperCase())
                .filter(otro -> !otro.getId().equals(idActual))
                .ifPresent(otro -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Ese código ya es de «" + otro.getNombre() + "».");
                });
    }

    private static void copiar(ProductoRequest d, Producto p) {
        p.setNombre(d.nombre().trim());
        p.setCategoria(d.categoria());
        p.setDescripcion(d.descripcion().trim());
        p.setPrecio(d.precio());
        p.setStock(d.stock());
        p.setPesoGramos(d.pesoGramos());
        p.setActivo(d.activo());
        p.setCodigo(d.codigo().trim().toUpperCase());
    }
}
