import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, Alert, StatusBar, RefreshControl,
} from 'react-native';
import { api } from '../api/config';

// HU02: Visualizar eventos gestionados por el instructor (con opcion de cancelar - US08)
// HU10: Visualizar eventos a los que asisto como aprendiz (con opcion de desinscribirse - US18)
export default function MisSesionesScreen({ navigation }: any) {
  const [misSesiones, setMisSesiones]    = useState<any[]>([]);
  const [misAsistencias, setAsistencias] = useState<any[]>([]);
  const [tab, setTab]                    = useState<'gestionadas' | 'asisto'>('gestionadas');
  const [loading, setLoading]            = useState(true);
  const [refreshing, setRefreshing]      = useState(false);

  const cargar = useCallback(async () => {
    let huboError = false;
    try {
      const gestionadas = await api.get('/sesiones/mis-sesiones');
      setMisSesiones(gestionadas.data.data || []);
    } catch (error: any) {
      setMisSesiones([]);
      if (error.response?.status !== 403) huboError = true;
    }
    try {
      const asistencias = await api.get('/inscripciones/mis-asistencias');
      setAsistencias(asistencias.data.data || []);
    } catch (error: any) {
      setAsistencias([]);
      if (error.response?.status !== 403) huboError = true;
    }
    if (huboError) Alert.alert('Error', 'No se pudieron cargar tus sesiones.');
    setLoading(false);
    setRefreshing(false);
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  // US08: Cancelar una sesion como instructor
  const handleCancelarSesion = (sesionId: number, titulo: string) => {
    Alert.alert(
      'Cancelar sesión',
      `¿Estás seguro de que quieres cancelar "${titulo}"? Esta accion no se puede deshacer.`,
      [
        { text: 'No', style: 'cancel' },
        {
          text: 'Sí, cancelar',
          style: 'destructive',
          onPress: async () => {
            try {
              await api.delete(`/sesiones/${sesionId}`);
              Alert.alert('Sesión cancelada', 'La sesión fue cancelada exitosamente.');
              cargar();
            } catch (error: any) {
              Alert.alert('Error', error.response?.data?.mensaje || 'No se pudo cancelar la sesión.');
            }
          },
        },
      ]
    );
  };

  // US18: Desinscribirse de una sesion publica
  const handleDesinscribirse = (sesionId: number, titulo: string) => {
    Alert.alert(
      'Cancelar asistencia',
      `¿Seguro que quieres salir de "${titulo}"?`,
      [
        { text: 'No', style: 'cancel' },
        {
          text: 'Sí, salir',
          style: 'destructive',
          onPress: async () => {
            try {
              await api.delete(`/inscripciones/${sesionId}/salir`);
              Alert.alert('Listo', 'Te has desinscrito de la sesión.');
              cargar();
            } catch (error: any) {
              Alert.alert('Error', error.response?.data?.mensaje || 'No se pudo desinscribir.');
            }
          },
        },
      ]
    );
  };

  const estadoColor: Record<string, string> = {
    ACTIVA: '#22C55E',
    PENDIENTE: '#F59E0B',
    CANCELADA: '#EF4444',
    FINALIZADA: '#8898AA',
  };

  const renderGestionada = ({ item }: any) => (
    <TouchableOpacity
      style={styles.card}
      onPress={() => navigation.navigate('DetalleSesion', { sesionId: item.sesionId })}
      activeOpacity={0.85}
    >
      <View style={styles.cardTopRow}>
        <View style={[styles.tipoBadge, item.tipo === 'PUBLICA' ? styles.tipoPublica : styles.tipoPrivada]}>
          <Text style={[styles.tipoBadgeTexto, item.tipo === 'PUBLICA' ? { color: '#F97316' } : { color: '#6366F1' }]}>
            {item.tipo === 'PUBLICA' ? 'Pública' : 'Privada'}
          </Text>
        </View>
        <View style={[styles.estadoPill, { backgroundColor: (estadoColor[item.estado] || '#8898AA') + '20' }]}>
          <View style={[styles.estadoPillDot, { backgroundColor: estadoColor[item.estado] || '#8898AA' }]} />
          <Text style={[styles.estadoPillTexto, { color: estadoColor[item.estado] || '#8898AA' }]}>{item.estado}</Text>
        </View>
      </View>
      <Text style={styles.cardTitulo} numberOfLines={2}>{item.titulo}</Text>
      <Text style={styles.cardFecha}>
        {item.fechaSesion ? new Date(item.fechaSesion).toLocaleDateString('es-PE', { weekday: 'short', day: '2-digit', month: 'short' }) : 'Por confirmar'}
      </Text>
      {item.estado !== 'CANCELADA' && item.estado !== 'FINALIZADA' && (
        <TouchableOpacity
          style={styles.accionBtn}
          onPress={() => handleCancelarSesion(item.sesionId, item.titulo)}
        >
          <Text style={styles.accionBtnTexto}>Cancelar sesión</Text>
        </TouchableOpacity>
      )}
    </TouchableOpacity>
  );

  const renderAsistencia = ({ item }: any) => (
    <TouchableOpacity
      style={styles.card}
      onPress={() => navigation.navigate('DetalleSesion', { sesionId: item.sesionId })}
      activeOpacity={0.85}
    >
      <View style={styles.cardTopRow}>
        <View style={[styles.estadoPill, { backgroundColor: (estadoColor[item.estado] || '#8898AA') + '20' }]}>
          <View style={[styles.estadoPillDot, { backgroundColor: estadoColor[item.estado] || '#8898AA' }]} />
          <Text style={[styles.estadoPillTexto, { color: estadoColor[item.estado] || '#8898AA' }]}>{item.estado}</Text>
        </View>
      </View>
      <Text style={styles.cardTitulo} numberOfLines={2}>{item.titulo}</Text>
      <Text style={styles.cardFecha}>
        {item.fechaSesion ? new Date(item.fechaSesion).toLocaleDateString('es-PE', { weekday: 'short', day: '2-digit', month: 'short' }) : 'Por confirmar'}
      </Text>
      <Text style={styles.cardInstructor}>{item.instructorNombre || 'Instructor'}</Text>
      {item.estado === 'ACTIVA' && (
        <TouchableOpacity
          style={[styles.accionBtn, styles.accionBtnDesinscribir]}
          onPress={() => handleDesinscribirse(item.sesionId, item.titulo)}
        >
          <Text style={[styles.accionBtnTexto, { color: '#DD6B20' }]}>Cancelar asistencia</Text>
        </TouchableOpacity>
      )}
    </TouchableOpacity>
  );

  const datos = tab === 'gestionadas' ? misSesiones : misAsistencias;

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0F1C36" />

      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Mis Sesiones</Text>
        {tab === 'gestionadas' && (
          <TouchableOpacity onPress={() => navigation.navigate('CrearSesion')}>
            <Text style={styles.crearTexto}>+ Nueva</Text>
          </TouchableOpacity>
        )}
        {tab !== 'gestionadas' && <View style={{ width: 50 }} />}
      </View>

      {/* Tabs */}
      <View style={styles.tabs}>
        <TouchableOpacity
          style={[styles.tab, tab === 'gestionadas' && styles.tabActivo]}
          onPress={() => setTab('gestionadas')}
        >
          <Text style={[styles.tabTexto, tab === 'gestionadas' && styles.tabTextoActivo]}>Gestiono</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, tab === 'asisto' && styles.tabActivo]}
          onPress={() => setTab('asisto')}
        >
          <Text style={[styles.tabTexto, tab === 'asisto' && styles.tabTextoActivo]}>Asisto</Text>
        </TouchableOpacity>
      </View>

      {loading ? (
        <ActivityIndicator size="large" color="#F97316" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={datos}
          keyExtractor={(item) => String(item.sesionId)}
          renderItem={tab === 'gestionadas' ? renderGestionada : renderAsistencia}
          contentContainerStyle={styles.lista}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); cargar(); }} colors={['#F97316']} />}
          ListEmptyComponent={
            <Text style={styles.vacio}>
              {tab === 'gestionadas' ? 'Todavía no has creado ninguna sesión.' : 'No estás inscrito en ninguna sesión.'}
            </Text>
          }
        />
      )}
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
  backTexto: { color: '#8898AA', fontSize: 14, fontWeight: '600' },
  headerTitulo: { color: '#FFFFFF', fontSize: 18, fontWeight: '800' },
  crearTexto: { color: '#F97316', fontSize: 14, fontWeight: '800' },
  tabs: {
    flexDirection: 'row',
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E8EDF2',
  },
  tab: { flex: 1, paddingVertical: 15, alignItems: 'center' },
  tabActivo: { borderBottomWidth: 3, borderBottomColor: '#F97316' },
  tabTexto: { fontSize: 14, fontWeight: '700', color: '#8898AA' },
  tabTextoActivo: { color: '#F97316' },
  lista: { padding: 16, paddingBottom: 40 },
  card: {
    backgroundColor: '#FFFFFF', borderRadius: 16, padding: 18, marginBottom: 12,
    shadowColor: '#0F1C36', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.07, shadowRadius: 8, elevation: 3,
  },
  cardTopRow: { flexDirection: 'row', gap: 8, marginBottom: 10, alignItems: 'center' },
  tipoBadge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 20 },
  tipoPublica: { backgroundColor: '#FFF0E6' },
  tipoPrivada: { backgroundColor: '#EEEEFF' },
  tipoBadgeTexto: { fontSize: 11, fontWeight: '800' },
  estadoPill: {
    flexDirection: 'row', alignItems: 'center', gap: 5,
    paddingHorizontal: 10, paddingVertical: 4, borderRadius: 20,
  },
  estadoPillDot: { width: 6, height: 6, borderRadius: 3 },
  estadoPillTexto: { fontSize: 11, fontWeight: '800' },
  cardTitulo: { fontSize: 15, fontWeight: '800', color: '#0F1C36', lineHeight: 21, marginBottom: 6 },
  cardFecha: { fontSize: 13, color: '#8898AA', fontWeight: '600', marginBottom: 4 },
  cardInstructor: { fontSize: 13, color: '#4A5568' },
  accionBtn: {
    marginTop: 12, alignSelf: 'flex-start',
    paddingVertical: 8, paddingHorizontal: 14,
    backgroundColor: '#FEEFEF', borderRadius: 10,
    borderWidth: 1.5, borderColor: '#EF4444',
  },
  accionBtnDesinscribir: { backgroundColor: '#FFF3E0', borderColor: '#DD6B20' },
  accionBtnTexto: { color: '#EF4444', fontWeight: '700', fontSize: 13 },
  vacio: { textAlign: 'center', color: '#8898AA', marginTop: 60, fontSize: 15 },
});
