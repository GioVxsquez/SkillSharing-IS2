import React, { useEffect, useState } from 'react';
import {
  View, Text, ScrollView, StyleSheet, TouchableOpacity,
  ActivityIndicator, Alert, StatusBar, TextInput,
} from 'react-native';
import { api } from '../api/config';

// HU04, HU17, HU26, US05/US27, US19/US20
export default function DetalleSesionScreen({ route, navigation }: any) {
  const { sesionId } = route.params;
  const [sesion, setSesion]           = useState<any>(null);
  const [invitados, setInvitados]     = useState<any[]>([]);
  const [materiales, setMateriales]   = useState<any[]>([]);
  const [calificaciones, setCals]     = useState<any[]>([]);
  const [loading, setLoading]         = useState(true);
  const [inscribiendo, setInscribiendo] = useState(false);
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
        Alert.alert('Error', 'No se pudo cargar la sesión.');
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
        Alert.alert('Listo', 'Te has inscrito exitosamente en esta sesión.');
      } else {
        Alert.alert('Aviso', resp.data.mensaje || 'No se pudo completar la inscripción.');
      }
    } catch (error: any) {
      const msj = error.response?.data?.mensaje || 'Error al inscribirse.';
      if (msj.toLowerCase().includes('cruce') || msj.toLowerCase().includes('horario')) {
        Alert.alert(
          'Cruce de horarios',
          'No puedes inscribirte porque ya tienes otra sesión confirmada que se cruza con este horario.\n\nRevisa tus sesiones en "Mis Sesiones".',
          [{ text: 'Entendido', style: 'cancel' }]
        );
      } else {
        Alert.alert('Error', msj);
      }
    } finally {
      setInscribiendo(false);
    }
  };

  const handleDescargar = async (materialId: number, nombre: string) => {
    Alert.alert('Descarga', `El archivo "${nombre}" se descargará. URL: ${api.defaults.baseURL}/materiales/${materialId}/descargar`);
  };

  const handleCalificar = async () => {
    if (puntuacion === 0) {
      Alert.alert('Faltan estrellas', 'Debes elegir una puntuación entre 1 y 5.');
      return;
    }
    setEnviandoCal(true);
    try {
      const resp = await api.post(`/calificaciones/sesion/${sesionId}`, { puntuacion, comentario });
      if (resp.data.ok) {
        Alert.alert('¡Gracias!', 'Tu calificación fue registrada.');
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

  const estadoColor: Record<string, string> = {
    ACTIVA: '#22C55E', PENDIENTE: '#F59E0B', CANCELADA: '#EF4444', FINALIZADA: '#8898AA'
  };

  if (loading) return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#F0F4F8' }}>
      <ActivityIndicator size="large" color="#F97316" />
    </View>
  );
  if (!sesion) return null;

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0F1C36" />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
          <Text style={styles.backTexto}>Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Detalle de Sesión</Text>
        <View style={{ width: 40 }} />
      </View>

      <ScrollView contentContainerStyle={styles.scroll}>

        {/* Hero Card */}
        <View style={styles.heroCard}>
          <View style={styles.cardTop}>
            <View style={[styles.tipoBadge, sesion.tipo === 'PUBLICA' ? styles.tipoPublica : styles.tipoPrivada]}>
              <Text style={[styles.tipoBadgeTexto, sesion.tipo === 'PUBLICA' ? { color: '#F97316' } : { color: '#6366F1' }]}>
                {sesion.tipo === 'PUBLICA' ? 'Pública' : 'Privada'}
              </Text>
            </View>
            <View style={[styles.estadoPill, { backgroundColor: (estadoColor[sesion.estado] || '#8898AA') + '20' }]}>
              <View style={[styles.estadoPillDot, { backgroundColor: estadoColor[sesion.estado] || '#8898AA' }]} />
              <Text style={[styles.estadoPillTexto, { color: estadoColor[sesion.estado] || '#8898AA' }]}>{sesion.estado}</Text>
            </View>
          </View>
          <Text style={styles.titulo}>{sesion.titulo}</Text>
          {sesion.categoria ? (
            <View style={styles.categoriaBadge}>
              <Text style={styles.categoriaTexto}>{sesion.categoria}</Text>
            </View>
          ) : null}
          
          <View style={styles.heroGrid}>
            <View style={styles.heroGridItem}>
              <Text style={styles.heroGridLabel}>Instructor</Text>
              <Text style={styles.heroGridValor} numberOfLines={1}>{sesion.instructorNombre || '-'}</Text>
            </View>
            <View style={styles.heroGridItem}>
              <Text style={styles.heroGridLabel}>Fecha</Text>
              <Text style={styles.heroGridValor} numberOfLines={1}>
                {sesion.fechaSesion ? new Date(sesion.fechaSesion).toLocaleDateString('es-PE', { day: '2-digit', month: 'short' }) : 'TBD'}
              </Text>
            </View>
            <View style={styles.heroGridItem}>
              <Text style={styles.heroGridLabel}>Modalidad</Text>
              <Text style={styles.heroGridValor} numberOfLines={1}>{sesion.modalidad || '-'}</Text>
            </View>
            <View style={styles.heroGridItem}>
              <Text style={styles.heroGridLabel}>Cupos</Text>
              <Text style={styles.heroGridValor} numberOfLines={1}>{sesion.maxParticipantes ? `Max ${sesion.maxParticipantes}` : 'Ilimitado'}</Text>
            </View>
          </View>
          {promedio && (
            <View style={styles.heroGridItemWide}>
              <Text style={styles.heroGridLabel}>Calificación de la sesión</Text>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 4 }}>
                <Text style={{ color: '#F59E0B', fontSize: 16 }}>★</Text>
                <Text style={styles.heroGridValor}>{promedio} / 5 ({calificaciones.length} reseñas)</Text>
              </View>
            </View>
          )}

          {sesion.descripcion ? (
            <View style={styles.descripcionWrap}>
              <Text style={styles.heroGridLabel}>De qué trata</Text>
              <Text style={styles.descripcion}>{sesion.descripcion}</Text>
            </View>
          ) : null}
        </View>

        {/* Materiales */}
        <View style={styles.seccion}>
          <Text style={styles.seccionTitulo}>Archivos adjuntos</Text>
          {materiales.length === 0 ? (
            <Text style={styles.vacioInline}>No hay materiales subidos para esta sesión.</Text>
          ) : (
            materiales.map((m) => (
              <TouchableOpacity key={String(m.materialId)} style={styles.filaItem} onPress={() => handleDescargar(m.materialId, m.nombre)}>
                <View style={styles.iconoDoc}><Text style={styles.iconoDocTexto}>DOC</Text></View>
                <Text style={styles.filaTextoMain}>{m.nombre}</Text>
                <Text style={styles.filaAccion}>Descargar</Text>
              </TouchableOpacity>
            ))
          )}
        </View>

        {/* Asistentes */}
        <View style={styles.seccion}>
          <Text style={styles.seccionTitulo}>Asistentes confirmados ({invitados.length})</Text>
          {invitados.length === 0 ? (
            <Text style={styles.vacioInline}>Nadie ha confirmado su asistencia todavía.</Text>
          ) : (
            invitados.map((item) => (
              <View key={String(item.usuarioId)} style={styles.filaItem}>
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

        {/* Calificar */}
        {sesion.estado === 'FINALIZADA' && (
          <View style={[styles.seccion, { backgroundColor: '#FFF0E6', borderColor: '#FEEBC8', borderWidth: 1 }]}>
            <Text style={[styles.seccionTitulo, { color: '#92400E' }]}>¿Qué te pareció la clase?</Text>
            <View style={styles.estrellas}>
              {[1, 2, 3, 4, 5].map((n) => (
                <TouchableOpacity key={n} onPress={() => setPuntuacion(n)}>
                  <Text style={[styles.estrella, n <= puntuacion && styles.estrellaActiva]}>{n <= puntuacion ? '★' : '☆'}</Text>
                </TouchableOpacity>
              ))}
            </View>
            <TextInput
              style={styles.comentarioInput}
              placeholder="Escribe tu opinión (opcional)"
              placeholderTextColor="#B0B8C1"
              value={comentario}
              onChangeText={setComentario}
              multiline
              numberOfLines={3}
            />
            <TouchableOpacity style={[styles.botonCalificar, enviandoCal && { opacity: 0.7 }]} onPress={handleCalificar} disabled={enviandoCal}>
              {enviandoCal ? <ActivityIndicator color="#fff" /> : <Text style={styles.botonCalificarTexto}>Enviar feedback</Text>}
            </TouchableOpacity>

            {calificaciones.length > 0 && (
              <View style={{ marginTop: 24, paddingTop: 16, borderTopWidth: 1, borderTopColor: '#FEEBC8' }}>
                <Text style={styles.resenasTitulo}>Opiniones ({calificaciones.length})</Text>
                {calificaciones.map((c, i) => (
                  <View key={i} style={styles.resenaCard}>
                    <Text style={styles.resenaEstrellas}>{'★'.repeat(c.puntuacion)}{'☆'.repeat(5 - c.puntuacion)}</Text>
                    {c.comentario ? <Text style={styles.resenaComentario}>{c.comentario}</Text> : null}
                  </View>
                ))}
              </View>
            )}
          </View>
        )}

        {/* Acciones base */}
        {sesion.tipo === 'PUBLICA' && sesion.estado === 'ACTIVA' && (
          <TouchableOpacity
            style={[styles.botonPrincipal, inscribiendo && { opacity: 0.7 }]}
            onPress={handleInscribirse}
            disabled={inscribiendo}
          >
            {inscribiendo ? <ActivityIndicator color="#fff" /> : <Text style={styles.botonPrincipalTexto}>Inscribirme ahora</Text>}
          </TouchableOpacity>
        )}

        {sesion.tipo === 'PRIVADA' && (
          <TouchableOpacity
            style={[styles.botonSecundario, { marginBottom: 12 }]}
            onPress={() => navigation.navigate('InvitarAsistentes', { sesionId: sesion.sesionId, sesionTitulo: sesion.titulo })}
          >
            <Text style={styles.botonSecundarioTexto}>Invitar personas</Text>
          </TouchableOpacity>
        )}

        <TouchableOpacity
          style={styles.botonOutline}
          onPress={() => navigation.navigate('SubirMaterial', { sesionId: sesion.sesionId, sesionTitulo: sesion.titulo })}
        >
          <Text style={styles.botonOutlineTexto}>Archivos de la clase</Text>
        </TouchableOpacity>

      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F0F4F8' },
  header: {
    backgroundColor: '#0F1C36',
    paddingTop: 52, paddingBottom: 20, paddingHorizontal: 20,
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
  },
  backBtn: { paddingVertical: 4 },
  backTexto: { color: '#8898AA', fontSize: 14, fontWeight: '600' },
  headerTitulo: { color: '#FFFFFF', fontSize: 18, fontWeight: '800' },
  scroll: { padding: 16, paddingBottom: 50 },
  heroCard: {
    backgroundColor: '#FFFFFF', borderRadius: 16, padding: 20, marginBottom: 16,
    shadowColor: '#0F1C36', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.08, shadowRadius: 10, elevation: 4,
  },
  cardTop: { flexDirection: 'row', gap: 10, marginBottom: 16 },
  tipoBadge: { paddingHorizontal: 12, paddingVertical: 5, borderRadius: 20 },
  tipoPublica: { backgroundColor: '#FFF0E6' },
  tipoPrivada: { backgroundColor: '#EEEEFF' },
  tipoBadgeTexto: { fontSize: 11, fontWeight: '900' },
  estadoPill: {
    flexDirection: 'row', alignItems: 'center', gap: 6,
    paddingHorizontal: 12, paddingVertical: 5, borderRadius: 20,
  },
  estadoPillDot: { width: 6, height: 6, borderRadius: 3 },
  estadoPillTexto: { fontSize: 11, fontWeight: '800' },
  titulo: { fontSize: 22, fontWeight: '900', color: '#0F1C36', marginBottom: 12, lineHeight: 28 },
  categoriaBadge: { backgroundColor: '#F0F4F8', borderRadius: 8, paddingHorizontal: 10, paddingVertical: 5, alignSelf: 'flex-start', marginBottom: 18 },
  categoriaTexto: { fontSize: 12, fontWeight: '700', color: '#4A5568' },
  heroGrid: { flexDirection: 'row', flexWrap: 'wrap', borderTopWidth: 1, borderTopColor: '#F0F4F8', paddingTop: 16 },
  heroGridItem: { width: '50%', marginBottom: 16 },
  heroGridItemWide: { width: '100%', marginBottom: 16 },
  heroGridLabel: { fontSize: 11, fontWeight: '700', color: '#8898AA', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 4 },
  heroGridValor: { fontSize: 14, color: '#0F1C36', fontWeight: '700' },
  descripcionWrap: { marginTop: 4 },
  descripcion: { fontSize: 14, color: '#4A5568', lineHeight: 22, marginTop: 6 },
  seccion: {
    backgroundColor: '#FFFFFF', borderRadius: 16, padding: 20, marginBottom: 16,
    shadowColor: '#0F1C36', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.05, shadowRadius: 6, elevation: 2,
  },
  seccionTitulo: { fontSize: 16, fontWeight: '800', color: '#0F1C36', marginBottom: 14 },
  vacioInline: { fontSize: 13, color: '#8898AA', fontStyle: 'italic' },
  filaItem: { flexDirection: 'row', alignItems: 'center', paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: '#F0F4F8' },
  iconoDoc: { width: 36, height: 36, borderRadius: 8, backgroundColor: '#FFF0E6', justifyContent: 'center', alignItems: 'center', marginRight: 12 },
  iconoDocTexto: { fontSize: 10, fontWeight: '900', color: '#F97316' },
  filaTextoMain: { fontSize: 14, color: '#0F1C36', fontWeight: '600', flex: 1 },
  filaAccion: { fontSize: 13, color: '#F97316', fontWeight: '800' },
  avatarMini: { width: 38, height: 38, borderRadius: 19, backgroundColor: '#0F1C36', justifyContent: 'center', alignItems: 'center', marginRight: 12 },
  avatarMiniTexto: { color: '#FFFFFF', fontWeight: '900', fontSize: 16 },
  invitadoNombre: { fontSize: 14, fontWeight: '800', color: '#0F1C36' },
  invitadoRol: { fontSize: 12, color: '#8898AA', marginTop: 2, fontWeight: '600' },
  estrellas: { flexDirection: 'row', gap: 6, marginBottom: 16, justifyContent: 'center' },
  estrella: { fontSize: 40, color: '#FEEBC8' },
  estrellaActiva: { color: '#F59E0B' },
  comentarioInput: {
    backgroundColor: '#FFFFFF', borderWidth: 1, borderColor: '#FEEBC8',
    borderRadius: 12, padding: 14, fontSize: 14, color: '#0F1C36',
    marginBottom: 16, textAlignVertical: 'top', minHeight: 80,
  },
  botonCalificar: { backgroundColor: '#F97316', paddingVertical: 14, borderRadius: 12, alignItems: 'center' },
  botonCalificarTexto: { color: '#FFFFFF', fontSize: 15, fontWeight: '800' },
  resenasTitulo: { fontSize: 14, fontWeight: '800', color: '#92400E', marginBottom: 12 },
  resenaCard: { backgroundColor: '#FFFFFF', padding: 12, borderRadius: 10, marginBottom: 8 },
  resenaEstrellas: { color: '#F59E0B', fontSize: 14, marginBottom: 6 },
  resenaComentario: { fontSize: 13, color: '#4A5568', lineHeight: 18 },
  botonPrincipal: {
    backgroundColor: '#F97316', paddingVertical: 16, borderRadius: 14, alignItems: 'center',
    shadowColor: '#F97316', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.35, shadowRadius: 10, elevation: 6,
    marginBottom: 12,
  },
  botonPrincipalTexto: { color: '#FFFFFF', fontSize: 16, fontWeight: '800' },
  botonSecundario: {
    backgroundColor: '#0F1C36', paddingVertical: 16, borderRadius: 14, alignItems: 'center',
  },
  botonSecundarioTexto: { color: '#FFFFFF', fontSize: 16, fontWeight: '800' },
  botonOutline: {
    backgroundColor: 'transparent', borderWidth: 2, borderColor: '#0F1C36',
    paddingVertical: 14, borderRadius: 14, alignItems: 'center',
  },
  botonOutlineTexto: { color: '#0F1C36', fontSize: 15, fontWeight: '800' },
});
