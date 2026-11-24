-- Catálogo de demostración (precios ficticios en pesos chilenos).
INSERT INTO productos (id, nombre, categoria, descripcion, precio, stock, peso_gramos, activo, codigo) VALUES
(1, 'Introducción a la museología', 'CURSO', 'Curso en línea de 8 semanas sobre funciones y ética de los museos.', 89000, 30, 0, TRUE, 'PRD-001'),
(2, 'Conservación preventiva de colecciones', 'CURSO', 'Control de humedad, luz y plagas en colecciones.', 120000, 20, 0, TRUE, 'PRD-002'),
(3, 'Manual de documentación de colecciones', 'PUBLICACION', 'Guía práctica para inventariar y catalogar objetos.', 24900, 40, 600, TRUE, 'PRD-003'),
(4, 'Revista Museos Iberoamericanos N.º 12', 'PUBLICACION', 'Número dedicado a museos virtuales y patrimonio digital.', 12500, 3, 300, TRUE, 'PRD-004'),
(5, 'Guantes de nitrilo (caja 100)', 'ARTICULO', 'Para manipular piezas sin dejar residuos.', 9900, 50, 400, TRUE, 'PRD-005'),
(6, 'Luxómetro para salas', 'ARTICULO', 'Mide la iluminación que reciben las obras.', 64900, 5, 250, TRUE, 'PRD-006'),
(7, 'Caja de archivo libre de ácido', 'CONSERVACION', 'Cartón neutro para documentos y textiles.', 15900, 25, 900, TRUE, 'PRD-007'),
(8, 'Papel tissue libre de ácido', 'CONSERVACION', 'SIN STOCK para demostrar la validación de disponibilidad.', 11900, 0, 500, TRUE, 'PRD-008'),
(9, 'Set de etiquetas museográficas', 'ARTICULO', 'Producto DESACTIVADO (gestión del Administrador).', 7500, 12, 150, FALSE, 'PRD-009');

-- Una compra de ejemplo de la Cliente Demo (usuario 3) para que la lista de solicitudes no parta vacía.
INSERT INTO solicitudes (usuario_id, cliente, tipo, resumen, subtotal, envio, total, destino, estado, fecha) VALUES
(3, 'Cliente Demo', 'Compra', '2 × Guantes de nitrilo (caja 100)', 19800, 4700, 24500, 'Nacional (Chile) · Estándar', 'CONFIRMADO', 1762300800000);
