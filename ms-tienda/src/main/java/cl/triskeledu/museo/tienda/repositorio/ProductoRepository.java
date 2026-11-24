package cl.triskeledu.museo.tienda.repositorio;

import cl.triskeledu.museo.tienda.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Consultas a la tabla productos.
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findAllByOrderByCategoriaAscNombreAsc();
    List<Producto> findByActivoTrueOrderByCategoriaAscNombreAsc();
    Optional<Producto> findByCodigo(String codigo);

    // Resta stock solo si alcanza y el producto está activo. Devuelve 0 si no se pudo (así dos compras no venden lo mismo).
    @Modifying
    @Query("UPDATE Producto p SET p.stock = p.stock - :cantidad WHERE p.id = :id AND p.activo = true AND p.stock >= :cantidad")
    int descontarStock(@Param("id") Long id, @Param("cantidad") int cantidad);
}
