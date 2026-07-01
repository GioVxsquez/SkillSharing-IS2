import React, { useEffect, useState } from 'react';
import {
  View, Text, ScrollView, StyleSheet, TouchableOpacity,
  ActivityIndicator, Alert, StatusBar, TextInput,
} from 'react-native';
import { api } from '../api/config';

// HU04: ver detalle de una sesion
// HU17: confirmar asistencia publica
// HU26: visualizar invitados confirmados
// US05/US27: materiales educativos
// US19/US20: calificaciones y reputacion
export default function DetalleSesionScreen({ route, navigation }: any) {
  const { sesionId } = route.params;
  const [sesion, setSesion]           = useState<any>(null);
  const [invitados, setInvitados]     = useState<any[]>([]);
  const [materiales, setMateriales]   = useState<any[]>([]);
  const [calificaciones, setCals]     = useState<any[]>([]);
  const [loading, setLoading]         = useState(true);
  const [inscribiendo, setInscribiendo] = useState(false);
  // Calificación
  const [puntuacion, setPuntuacion]   = useState(0);
  const [comentario, setComentario]   = useState('');
  const [enviandoCal, setEnviandoCal] = useState(false);

  useEffect(() => {
    const cargar = async () => {
      try {
        const [respSesion, respInvitados, respMateriales, respCals] = await Promise.all([
          api.get(`/sesiones/${sesionId}`),
          api.get(`/sesiones/${sesionId}/invitados`),
          api.get(`/materiales/sesion/${sesionId}`),
          api.get(`/calificaciones/sesion/${sesionId}`),
        ]);
        setSesion(respSesion.data.data);
        setInvitados(respInvitados.data.data || []);
        setMateriales(respMateriales.data.data || []);
        setCals(respCals.data.data || []);
      } catch {
        Alert.alert('Error', 'No se pudo cargar la sesion.');
        navigation.goBack();
      } finally {
        setLoading(false);
      }
    };
    cargar();
  }, [sesionId, navigation]);

  const handleInscribirse = async () => {
    setInscribiendo(true);
    try {
      const resp = await api.post(`/sesiones/${sesionId}/inscribirse`);
      if (resp.data.ok) {
        Alert.alert('Listo', 'Te has inscrito exitosamente en esta sesion.');
      } else {
        Alert.alert('Aviso', resp.data.mensaje || 'No se pudo completar la inscripcion.');
      }
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.mensaje || 'Error al inscribirse.');
    } finally {
      setInscribiendo(false);
    }
  };

  const handleDescargar = async (materialId: number, nombre: string) => {
    Alert.alert('Descarga', `El archivo "${nombre}" se descargará desde el servidor. URL: ${api.defaults.baseURL}/materiales/${materialId}/descargar`);
  };

  const handleCalificar = async () => {
    if (puntuacion === 0) {
      Alert.alert('Selecciona estrellas', 'Debes elegir una puntuación entre 1 y 5.');
      return;
    }
    setEnviandoCal(true);
    try {
      const resp = await api.post(`/calificaciones/sesion/${sesionId}`, { puntuacion, comentario });
      if (resp.data.ok) {
        Alert.alert('Gracias', 'Tu calificación fue registrada.');
        setCals((prev) => [...prev, resp.data.data]);
        setPuntuacion(0);
        setComentario('');
      }
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.mensaje || 'No se pudo enviar la calificación.');
    } finally {
      setEnviandoCal(false);
    }
  };

  const promedio = calificaciones.length > 0
    ? (calificaciones.reduce((sum, c) => sum + c.puntuacion, 0) / calificaciones.length).toFixed(1)
    : null;

  if (loading) return <ActivityIndicator style={{ flex: 1 }} size="large" color="#1B3A6B" />;
  if (!sesion) return null;

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1B3A6B" />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
          <Text style={styles.backTexto}>Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Detalle de Sesion</Text>
      </View>

      <ScrollView contentContainerStyle={styles.scroll}>

        {/* Info principal */}
        <View style={styles.card}>
          <View style={styles.cardTop}>
            <View style={[styles.badge, sesion.tipo === 'PUBLICA' ? styles.badgePublica : styles.badgePrivada]}>
              <Text style={styles.badgeTexto}>{sesion.tipo === 'PUBLICA' ? 'Publica' : 'Privada'}</Text>
            </View>
            <View style={[styles.badge, styles.badgeEstado]}>
              <Text style={styles.badgeTexto}>{sesion.estado}</Text>
            </View>
          </View>
          <Text style={styles.titulo}>{sesion.titulo}</Text>
          {sesion.categoria ? (
            <View style={styles.categoriaBadge}>
              <Text style={styles.categoriaTexto}>🏷️ {sesion.categoria}</Text>
            </View>
          ) : null}
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Instructor</Text>
            <Text style={styles.infoValor}>{sesion.instructorNombre || '-'}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Fecha</Text>
            <Text style={styles.infoValor}>
              {sesion.fechaSesion ? new Date(sesion.fechaSesion).toLocaleDateString('es-PE', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) : 'Por confirmar'}
            </Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Modalidad</Text>
            <Text style={styles.infoValor}>{sesion.modalidad || '-'}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Capacidad</Text>
            <Text style={styles.infoValor}>{sesion.maxParticipantes ? `Max. ${sesion.maxParticipantes} personas` : 'Sin limite'}</Text>
          </View>
          {promedio && (
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>⭐ Calificación</Text>
              <Text style={styles.infoValor}>{promedio} / 5 ({calificaciones.length} reseñas)</Text>
            </View>
          )}
          {sesion.descripcion ? (
            <View style={styles.descripcionContainer}>
              <Text style={styles.infoLabel}>Descripcion</Text>
              <Text style={styles.descripcion}>{sesion.descripcion}</Text>
            </View>
          ) : null}
        </View>

        {/* Materiales educativos - US27 */}
        <View style={styles.card}>
          <Text style={styles.seccionTitulo}>📎 Materiales educativos</Text>
          {materiales.length === 0 ? (
            <Text style={styles.vacioInline}>Esta sesion aun no tiene materiales subidos.</Text>
          ) : (
            materiales.map((m) => (
              <TouchableOpacity
                key={String(m.materialId)}
                style={styles.materialRow}
                onPress={() => handleDescargar(m.materialId, m.nombre)}
              >
                <Text style={styles.materialNombre}>📄 {m.nombre}</Text>
                <Text style={styles.descargarTexto}>Descargar</Text>
              </TouchableOpacity>
            ))
          )}
        </View>

        {/* Invitados confirmados */}
        <View style={styles.card}>
          <Text style={styles.seccionTitulo}>Asistentes confirmados</Text>
          {invitados.length === 0 ? (
            <Text style={styles.vacioInline}>Aun no hay asistentes confirmados para esta sesion.</Text>
          ) : (
            invitados.map((item) => (
              <View key={String(item.usuarioId)} style={styles.invitadoRow}>
                <View style={styles.avatarMini}>
                  <Text style={styles.avatarMiniTexto}>{item.nombre?.charAt(0)?.toUpperCase() || '?'}</Text>
                </View>
                <View style={{ flex: 1 }}>
                  <Text style={styles.invitadoNombre}>{item.nombre}</Text>
                  <Text style={styles.invitadoRol}>{item.rolSesion}</Text>
                </View>
              </View>
            ))
          )}
        </View>

        {/* Calificaciones - US19/US20 */}
        {sesion.estado === 'FINALIZADA' && (
          <View style={styles.card}>
            <Text style={styles.seccionTitulo}>⭐ Calificar esta sesion</Text>
            <View style={styles.estrellas}>
              {[1, 2, 3, 4, 5].map((n) => (
                <TouchableOpacity key={n} onPress={() => setPuntuacion(n)}>
                  <Text style={[styles.estrella, n <= puntuacion && styles.estrellaActiva]}>{n <= puntuacion ? '★' : '☆'}</Text>
                </TouchableOpacity>
              ))}
            </View>
            <TextInput
              style={styles.comentarioInput}
              placeholder="Escribe un comentario (opcional)..."
              placeholderTextColor="#A0AEC0"
              value={comentario}
              onChangeText={setComentario}
              multiline
              numberOfLines={3}
            />
            <TouchableOpacity style={[styles.boton, enviandoCal && { opacity: 0.7 }]} onPress={handleCalificar} disabled={enviandoCal}>
              {enviandoCal ? <ActivityIndicator color="#fff" /> : <Text style={styles.botonTexto}>Enviar calificación</Text>}
            </TouchableOpacity>
            {calificaciones.length > 0 && (
              <View style={{ marginTop: 16 }}>
                <Text style={[styles.seccionTitulo, { fontSize: 14 }]}>Reseñas ({calificaciones.length})</Text>
                {calificaciones.map((c, i) => (
                  <View key={i} style={styles.resenaRow}>
                    <Text style={styles.resenaEstrellas}>{'★'.repeat(c.puntuacion)}{'☆'.repeat(5 - c.puntuacion)}</Text>
                    {c.comentario ? <Text style={styles.resenaComentario}>{c.comentario}</Text> : null}
                  </View>
                ))}
              </View>
            )}
          </View>
        )}

        {/* Botones de accion */}
        {sesion.tipo === 'PUBLICA' && sesion.estado === 'ACTIVA' && (
          <TouchableOpacity
            style={[styles.boton, inscribiendo && { opacity: 0.7 }]}
            onPress={handleInscribirse}
            disabled={inscribiendo}
          >
            {inscribiendo ? <ActivityIndicator color="#fff" /> : <Text style={styles.botonTexto}>Confirmar Asistencia</Text>}
          </TouchableOpacity>
        )}

        {sesion.tipo === 'PRIVADA' && (
          <TouchableOpacity
            style={[styles.boton, { backgroundColor: '#FFFFFF', borderWidth: 2, borderColor: '#1B3A6B' }]}
            onPress={() => navigation.navigate('InvitarAsistentes', { sesionId: sesion.sesionId, sesionTitulo: sesion.titulo })}
          >
            <Text style={[styles.botonTexto, { color: '#1B3A6B' }]}>Invitar Asistentes</Text>
          </TouchableOpacity>
        )}

        {/* US05: boton para que el instructor gestione los materiales de su sesion */}
        <TouchableOpacity
          style={[styles.boton, { backgroundColor: '#2D3748', marginTop: 4 }]}
          onPress={() => navigation.navigate('SubirMaterial', { sesionId: sesion.sesionId, sesionTitulo: sesion.titulo })}
        >
          <Text style={styles.botonTexto}>📎 Gestionar materiales</Text>
        </TouchableOpacity>

      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F9FC' },
  header: { backgroundColor: '#1B3A6B', paddingTop: 48, paddingBottom: 16, paddingHorizontal: 20 },
  backBtn: { marginBottom: 6 },
  backTexto: { color: '#C8D8F0', fontSize: 14 },
  headerTitulo: { color: '#FFFFFF', fontSize: 20, fontWeight: '700' },
  scroll: { padding: 16, paddingBottom: 40 },
  card: { backgroundColor: '#FFFFFF', borderRadius: 14, padding: 20, marginBottom: 16, shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.07, shadowRadius: 6, elevation: 3 },
  cardTop: { flexDirection: 'row', gap: 8, marginBottom: 14 },
  badge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 20 },
  badgePublica: { backgroundColor: '#E6F4FF' },
  badgePrivada: { backgroundColor: '#FFF3E0' },
  badgeEstado: { backgroundColor: '#E8F5E9' },
  badgeTexto: { fontSize: 12, fontWeight: '700', color: '#1B3A6B' },
  titulo: { fontSize: 20, fontWeight: '800', color: '#1A202C', marginBottom: 10, lineHeight: 26 },
  categoriaBadge: { backgroundColor: '#EBF4FF', borderRadius: 8, paddingHorizontal: 10, paddingVertical: 4, alignSelf: 'flex-start', marginBottom: 14 },
  categoriaTexto: { fontSize: 13, fontWeight: '600', color: '#1B3A6B' },
  infoRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: '#F0F4F8' },
  infoLabel: { fontSize: 13, fontWeight: '600', color: '#718096' },
  infoValor: { fontSize: 13, color: '#2D3748', fontWeight: '500', textAlign: 'right', flex: 1, marginLeft: 16 },
  descripcionContainer: { marginTop: 14 },
  descripcion: { fontSize: 14, color: '#4A5568', lineHeight: 22, marginTop: 6 },
  seccionTitulo: { fontSize: 16, fontWeight: '800', color: '#1B3A6B', marginBottom: 12 },
  vacioInline: { fontSize: 13, color: '#718096', lineHeight: 20 },
  materialRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: '#F0F4F8' },
  materialNombre: { fontSize: 13, color: '#2D3748', flex: 1 },
  descargarTexto: { fontSize: 13, color: '#1B3A6B', fontWeight: '700' },
  invitadoRow: { flexDirection: 'row', alignItems: 'center', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: '#F0F4F8' },
  avatarMini: { width: 34, height: 34, borderRadius: 17, backgroundColor: '#1B3A6B', justifyContent: 'center', alignItems: 'center', marginRight: 10 },
  avatarMiniTexto: { color: '#FFD700', fontWeight: '800' },
  invitadoNombre: { fontSize: 14, fontWeight: '700', color: '#1A202C' },
  invitadoRol: { fontSize: 12, color: '#718096', marginTop: 2 },
  estrellas: { flexDirection: 'row', gap: 8, marginBottom: 14, justifyContent: 'center' },
  estrella: { fontSize: 32, color: '#CBD5E0' },
  estrellaActiva: { color: '#FFD700' },
  comentarioInput: { borderWidth: 1.5, borderColor: '#E2E8F0', borderRadius: 10, padding: 12, fontSize: 14, color: '#1A202C', marginBottom: 14, textAlignVertical: 'top', minHeight: 70 },
  resenaRow: { paddingVertical: 8, borderTopWidth: 1, borderTopColor: '#F0F4F8' },
  resenaEstrellas: { color: '#FFD700', fontSize: 16, marginBottom: 4 },
  resenaComentario: { fontSize: 13, color: '#4A5568' },
  boton: { backgroundColor: '#1B3A6B', paddingVertical: 16, borderRadius: 12, alignItems: 'center', shadowColor: '#1B3A6B', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.25, shadowRadius: 8, elevation: 5, marginBottom: 12 },
  botonTexto: { color: '#FFFFFF', fontSize: 16, fontWeight: '700' },
});
