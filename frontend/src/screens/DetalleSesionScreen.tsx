import React, { useEffect, useState } from 'react';
import {
  View, Text, ScrollView, StyleSheet, TouchableOpacity,
  ActivityIndicator, Alert, StatusBar,
} from 'react-native';
import { api } from '../api/config';

// HU04: ver detalle de una sesion
// HU17: confirmar asistencia publica
// HU26: visualizar invitados confirmados
export default function DetalleSesionScreen({ route, navigation }: any) {
  const { sesionId } = route.params;
  const [sesion, setSesion] = useState<any>(null);
  const [invitados, setInvitados] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [inscribiendo, setInscribiendo] = useState(false);

  useEffect(() => {
    const cargar = async () => {
      try {
        const [respSesion, respInvitados] = await Promise.all([
          api.get(`/sesiones/${sesionId}`),
          api.get(`/sesiones/${sesionId}/invitados`),
        ]);
        setSesion(respSesion.data.data);
        setInvitados(respInvitados.data.data || []);
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
            <Text style={styles.infoLabel}>Duracion</Text>
            <Text style={styles.infoValor}>{sesion.duracionMinutos ? `${sesion.duracionMinutos} minutos` : '-'}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Ubicacion</Text>
            <Text style={styles.infoValor}>{sesion.ubicacion || 'Virtual'}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Capacidad</Text>
            <Text style={styles.infoValor}>{sesion.capacidadMaxima ? `Max. ${sesion.capacidadMaxima} personas` : 'Sin limite'}</Text>
          </View>

          {sesion.descripcion ? (
            <View style={styles.descripcionContainer}>
              <Text style={styles.infoLabel}>Descripcion</Text>
              <Text style={styles.descripcion}>{sesion.descripcion}</Text>
            </View>
          ) : null}
        </View>

        <View style={styles.card}>
          <Text style={styles.seccionTitulo}>Invitados confirmados</Text>
          {invitados.length === 0 ? (
            <Text style={styles.vacioInline}>Aun no hay invitados confirmados para esta sesion.</Text>
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

        {sesion.tipo === 'PUBLICA' && sesion.estado === 'ACTIVA' && (
          <TouchableOpacity
            style={[styles.boton, inscribiendo && { opacity: 0.7 }]}
            onPress={handleInscribirse}
            disabled={inscribiendo}
          >
            {inscribiendo
              ? <ActivityIndicator color="#fff" />
              : <Text style={styles.botonTexto}>Confirmar Asistencia</Text>}
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
  seccionTitulo: { fontSize: 16, fontWeight: '800', color: '#1B3A6B', marginBottom: 12 },
  vacioInline: { fontSize: 13, color: '#718096', lineHeight: 20 },
  invitadoRow: { flexDirection: 'row', alignItems: 'center', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: '#F0F4F8' },
  avatarMini: { width: 34, height: 34, borderRadius: 17, backgroundColor: '#1B3A6B', justifyContent: 'center', alignItems: 'center', marginRight: 10 },
  avatarMiniTexto: { color: '#FFD700', fontWeight: '800' },
  invitadoNombre: { fontSize: 14, fontWeight: '700', color: '#1A202C' },
  invitadoRol: { fontSize: 12, color: '#718096', marginTop: 2 },
  boton: { backgroundColor: '#1B3A6B', paddingVertical: 16, borderRadius: 12, alignItems: 'center', shadowColor: '#1B3A6B', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.25, shadowRadius: 8, elevation: 5, marginBottom: 12 },
  botonTexto: { color: '#FFFFFF', fontSize: 16, fontWeight: '700' },
});
