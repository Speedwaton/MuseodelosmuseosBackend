package cl.triskeledu.museo.tienda.servicio;

import cl.triskeledu.museo.tienda.dto.Dtos.ProductoRequest;
import cl.triskeledu.museo.tienda.error.ApiException;
import cl.triskeledu.museo.tienda.modelo.Categoria;
import cl.triskeledu.museo.tienda.modelo.Producto;
import cl.triskeledu.museo.tienda.repositorio.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas del catálogo con el repositorio simulado.
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    ProductoRepository repositorio;

    @InjectMocks
    ProductoService servicio;

    @Test
    void listar_paraCliente_soloActivos() {
        servicio.listar(true);
        verify(repositorio).findByActivoTrueOrderByCategoriaAscNombreAsc();
    }

    @Test
    void porCodigo_productoDesactivado_responde404() {
        Producto p = new Producto(9L, "Set de etiquetas", Categoria.ARTICULO, 7_500, 12, 150);
        p.setActivo(false);
        when(repositorio.findByCodigo("PRD-009")).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> servicio.porCodigo("prd-009"))
                .extracting(e -> ((ApiException) e).getEstado()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void crear_guardaElCodigoEnMayusculas() {
        when(repositorio.findByCodigo("PRD-010")).thenReturn(Optional.empty());
        when(repositorio.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        Producto p = servicio.crear(new ProductoRequest("Higrómetro digital", Categoria.ARTICULO,
                "Mide la humedad relativa de las salas.", 29_900, 8, 200, true, "prd-010"));

        assertThat(p.getCodigo()).isEqualTo("PRD-010");
        assertThat(p.getPrecio()).isEqualTo(29_900);
    }

    @Test
    void actualizar_conCodigoDeOtroProducto_responde409() {
        Producto otro = new Producto(6L, "Luxómetro para salas", Categoria.ARTICULO, 64_900, 5, 250);
        when(repositorio.findById(5L)).thenReturn(Optional.of(new Producto(5L, "Guantes", Categoria.ARTICULO, 9_900, 50, 400)));
        when(repositorio.findByCodigo("PRD-006")).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> servicio.actualizar(5L, new ProductoRequest("Guantes", Categoria.ARTICULO,
                "Para manipular piezas sin residuos.", 9_900, 50, 400, true, "PRD-006")))
                .hasMessage("Ese código ya es de «Luxómetro para salas».");
    }

    @Test
    void cambiarActivo_desactivaSinBorrar() {
        Producto p = new Producto(6L, "Luxómetro", Categoria.ARTICULO, 64_900, 5, 250);
        when(repositorio.findById(6L)).thenReturn(Optional.of(p));
        when(repositorio.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(servicio.cambiarActivo(6L, false).isActivo()).isFalse();
    }
}
