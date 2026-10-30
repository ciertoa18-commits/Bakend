-- Ejecutar conectado como FERRE. No ejecutar las migraciones manualmente.
SELECT 'propietarios' tabla, COUNT(*) cantidad FROM propietario
UNION ALL SELECT 'prestaciones', COUNT(*) FROM prestacion
UNION ALL SELECT 'mascotas', COUNT(*) FROM mascota
UNION ALL SELECT 'atenciones', COUNT(*) FROM atencion;
SELECT a.numero,a.fecha,m.nombre,m.especie,a.estado,a.subtotal,a.descuento,a.total
FROM atencion a JOIN mascota m ON m.id=a.mascota_id ORDER BY a.id DESC;
SELECT a.numero,p.codigo,p.especie_aplicable,d.cantidad,d.precio_unitario,d.subtotal
FROM detalle_atencion d JOIN atencion a ON a.id=d.atencion_id
JOIN prestacion p ON p.id=d.prestacion_id ORDER BY a.id,d.id;
-- Debe devolver cero filas: totales de cabecera consistentes con las lineas y el descuento.
SELECT a.numero FROM atencion a JOIN detalle_atencion d ON d.atencion_id=a.id
GROUP BY a.numero,a.subtotal,a.descuento,a.total
HAVING a.subtotal<>SUM(d.subtotal) OR a.total<>a.subtotal-a.descuento
 OR a.descuento<>CASE WHEN COUNT(DISTINCT d.prestacion_id)>=3 THEN ROUND(a.subtotal*0.1,2) ELSE 0 END;
SELECT m.especie,COUNT(*) cantidad_atenciones,SUM(a.total) importe_total
FROM atencion a JOIN mascota m ON m.id=a.mascota_id
WHERE a.estado='REGISTRADA' GROUP BY m.especie ORDER BY importe_total DESC;
-- Tomar ambos conteos antes y despues del POST con PRS-004 como tercer detalle de Firulais.
SELECT COUNT(*) cabeceras FROM atencion;
SELECT COUNT(*) detalles FROM detalle_atencion;
-- Ambos conteos deben permanecer iguales tras recibir 409.
