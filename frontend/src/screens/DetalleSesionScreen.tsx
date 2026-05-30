import React, { useEffect, useState } from 'react';
import {
  View, Text, ScrollView, StyleSheet, TouchableOpacity,
  ActivityIndicator, Alert, StatusBar,
} from 'react-native';
import { api } from '../api/config';

// HU04: Ver detalle de un evento/sesión
// HU17: Confirmar asistencia pública
export default function DetalleSesionScreen({ route, navigation }: any) {
  const { sesionId } = route.params;
  const [sesion, setSesion]         = useState<any>(null);
  const [loading, setLoading]       = useState(true);
  const [inscribiendo, setInscribiendo] = useState(false);

  useEffect(() => {
    const cargar = async () => {
      try {
        const resp = await api.get(`/sesiones/${sesionId}`);
        setSesion(resp.data.data);
      } catch {
        Alert.alert('Error', 'No se pudo cargar la sesión.');
        navigation.goBack();
      } finally {
        setLoading(false);
      }
    };
    cargar();
  }, [sesionId]);

  // HU17: confirmar asistencia a sesión pública
  const handleInscribirse = async () => {
    setInscribiendo(true);
    try {
      const resp = await api.post(`/sesiones/${sesionId}/inscribirse`);
      if (resp.data.ok) {
        Alert.alert('¡Listo!', 'Te has inscrito exitosamente en esta sesión.');
      } else {
        Alert.alert('Aviso', resp.data.mensaje || 'No se pudo completar la inscripción.');
      }
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.mensaje || 'Error al inscribirse.');
    } finally {
      setInscribiendo(false);
    }
  };

  if (loading) return <ActivityIndicator style={{ flex: 1 }} size="large" color="#1B3A6B" />;
  if (!sesion) return null;

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1B3A6B" />

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
          <Text style={styles.backTexto}>← Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Detalle de Sesión</Text>
      </View>

      <ScrollView contentContainerStyle={styles.scroll}>
        {/* Tarjeta principal */}
        <View style={styles.card}>
          <View style={styles.cardTop}>
            <View style={[styles.badge, sesion.tipo === 'PUBLICA' ? styles.badgePublica : styles.badgePrivada]}>
              <Text style={styles.badgeTexto}>{sesion.tipo === 'PUBLICA' ? '🌐 Pública' : '🔒 Privada'}</Text>
            </View>
            <View style={[styles.badge, styles.badgeEstado]}>
              <Text style={styles.badgeTexto}>{sesion.estado}</Text>
            </View>
          </View>

          <Text style={styles.titulo}>{sesion.titulo}</Text>

          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>👤 Instructor</Text>
            <Text style={styles.infoValor}>{sesion.instructorNombre || '—'}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>📅 Fecha</Text>
            <Text style={styles.infoValor}>
              {sesion.fechaSesion ? new Date(sesion.fechaSesion).toLocaleDateString('es-PE', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) : 'Por confirmar'}
            </Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>⏱ Duración</Text>
            <Text style={styles.infoValor}>{sesion.duracionMinutos ? `${sesion.duracionMinutos} minutos` : '—'}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>📍 Ubicación</Text>
            <Text style={styles.infoValor}>{sesion.ubicacion || 'Virtual'}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>👥 Capacidad</Text>
            <Text style={styles.infoValor}>{sesion.capacidadMaxima ? `Máx. ${sesion.capacidadMaxima} personas` : 'Sin límite'}</Text>
          </View>

          {sesion.descripcion ? (
            <View style={styles.descripcionContainer}>
              <Text style={styles.infoLabel}>📝 Descripción</Text>
              <Text style={styles.descripcion}>{sesion.descripcion}</Text>
            </View>
          ) : null}
        </View>

        {/* Botón inscribirse (solo sesiones públicas activas) */}
        {sesion.tipo === 'PUBLICA' && sesion.estado === 'ACTIVA' && (
          <TouchableOpacity
            style={[styles.boton, inscribiendo && { opacity: 0.7 }]}
            onPress={handleInscribirse}
            disabled={inscribiendo}
          >
            {inscribiendo
              ? <ActivityIndicator color="#fff" />
              : <Text style={styles.botonTexto}>✅ Confirmar Asistencia</Text>}
          </TouchableOpacity>
        )}

        {/* Botón invitar (solo para sesiones privadas del instructor) */}
        {/* Como no tenemos el ID del usuario en contexto global facilmente, 
            asumimos que si es privada, damos la opción de invitar. 
            En una app real, verificaríamos que sesion.instructorId === miUsuarioId */}
        {sesion.tipo === 'PRIVADA' && (
          <TouchableOpacity
            style={[styles.boton, { backgroundColor: '#FFFFFF', borderWidth: 2, borderColor: '#1B3A6B' }]}
            onPress={() => navigation.navigate('InvitarAsistentes', { sesionId: sesion.sesionId, sesionTitulo: sesion.titulo })}
          >
            <Text style={[styles.botonTexto, { color: '#1B3A6B' }]}>✉️ Invitar Asistentes</Text>
          </TouchableOpacity>
        )}
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
  titulo: { fontSize: 20, fontWeight: '800', color: '#1A202C', marginBottom: 18, lineHeight: 26 },
  infoRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: '#F0F4F8' },
  infoLabel: { fontSize: 13, fontWeight: '600', color: '#718096' },
  infoValor: { fontSize: 13, color: '#2D3748', fontWeight: '500', textAlign: 'right', flex: 1, marginLeft: 16 },
  descripcionContainer: { marginTop: 14 },
  descripcion: { fontSize: 14, color: '#4A5568', lineHeight: 22, marginTop: 6 },
  boton: { backgroundColor: '#1B3A6B', paddingVertical: 16, borderRadius: 12, alignItems: 'center', shadowColor: '#1B3A6B', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.25, shadowRadius: 8, elevation: 5 },
  botonTexto: { color: '#FFFFFF', fontSize: 16, fontWeight: '700' },
});
