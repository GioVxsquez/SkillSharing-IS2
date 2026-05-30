import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, Alert, StatusBar, RefreshControl,
} from 'react-native';
import { api } from '../api/config';

// HU02: Visualizar eventos gestionados por el instructor
// HU10: Visualizar eventos a los que asisto como aprendiz
export default function MisSesionesScreen({ navigation }: any) {
  const [misSesiones, setMisSesiones]   = useState<any[]>([]);
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
      if (error.response?.status !== 403) {
        huboError = true;
      }
    }

    try {
      const asistencias = await api.get('/inscripciones/mis-asistencias');
      setAsistencias(asistencias.data.data || []);
    } catch (error: any) {
      setAsistencias([]);
      if (error.response?.status !== 403) {
        huboError = true;
      }
    }

    if (huboError) {
      Alert.alert('Error', 'No se pudieron cargar tus sesiones.');
    }

    setLoading(false);
    setRefreshing(false);
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  const datos = tab === 'gestionadas' ? misSesiones : misAsistencias;

  const renderItem = ({ item }: any) => (
    <TouchableOpacity
      style={styles.card}
      onPress={() => navigation.navigate('DetalleSesion', { sesionId: item.sesionId })}
    >
      <Text style={styles.cardTitulo}>{item.titulo}</Text>
      <Text style={styles.cardInfo}>📅 {item.fechaSesion ? new Date(item.fechaSesion).toLocaleDateString('es-PE') : 'Por confirmar'}</Text>
      <View style={styles.cardFooter}>
        <View style={[styles.badge, item.tipo === 'PUBLICA' ? styles.badgePublica : styles.badgePrivada]}>
          <Text style={styles.badgeTexto}>{item.tipo}</Text>
        </View>
        <View style={[styles.badge, styles.badgeEstado]}>
          <Text style={styles.badgeTexto}>{item.estado}</Text>
        </View>
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1B3A6B" />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>← Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Mis Sesiones</Text>
      </View>

      {/* Tabs */}
      <View style={styles.tabs}>
        <TouchableOpacity style={[styles.tab, tab === 'gestionadas' && styles.tabActivo]} onPress={() => setTab('gestionadas')}>
          <Text style={[styles.tabTexto, tab === 'gestionadas' && styles.tabTextoActivo]}>📋 Que gestiono</Text>
        </TouchableOpacity>
        <TouchableOpacity style={[styles.tab, tab === 'asisto' && styles.tabActivo]} onPress={() => setTab('asisto')}>
          <Text style={[styles.tabTexto, tab === 'asisto' && styles.tabTextoActivo]}>🙋 Que asisto</Text>
        </TouchableOpacity>
      </View>

      {loading ? (
        <ActivityIndicator size="large" color="#1B3A6B" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={datos}
          keyExtractor={(item) => String(item.sesionId)}
          renderItem={renderItem}
          contentContainerStyle={styles.lista}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); cargar(); }} colors={['#1B3A6B']} />}
          ListEmptyComponent={<Text style={styles.vacio}>{tab === 'gestionadas' ? 'No has creado ninguna sesión.' : 'No estás inscrito en ninguna sesión.'}</Text>}
        />
      )}

      {/* FAB crear */}
      {tab === 'gestionadas' && (
        <TouchableOpacity style={styles.fab} onPress={() => navigation.navigate('CrearSesion')}>
          <Text style={styles.fabTexto}>+</Text>
        </TouchableOpacity>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F9FC' },
  header: { backgroundColor: '#1B3A6B', paddingTop: 48, paddingBottom: 16, paddingHorizontal: 20 },
  backTexto: { color: '#C8D8F0', fontSize: 14, marginBottom: 6 },
  headerTitulo: { color: '#FFFFFF', fontSize: 20, fontWeight: '700' },
  tabs: { flexDirection: 'row', backgroundColor: '#FFFFFF', borderBottomWidth: 1, borderBottomColor: '#E2E8F0' },
  tab: { flex: 1, paddingVertical: 14, alignItems: 'center' },
  tabActivo: { borderBottomWidth: 3, borderBottomColor: '#1B3A6B' },
  tabTexto: { fontSize: 14, fontWeight: '600', color: '#718096' },
  tabTextoActivo: { color: '#1B3A6B' },
  lista: { padding: 16, paddingBottom: 80 },
  card: { backgroundColor: '#FFFFFF', borderRadius: 14, padding: 16, marginBottom: 12, shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.06, shadowRadius: 6, elevation: 2 },
  cardTitulo: { fontSize: 15, fontWeight: '700', color: '#1A202C', marginBottom: 6 },
  cardInfo: { fontSize: 13, color: '#4A5568', marginBottom: 10 },
  cardFooter: { flexDirection: 'row', gap: 8 },
  badge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 20 },
  badgePublica: { backgroundColor: '#E6F4FF' },
  badgePrivada: { backgroundColor: '#FFF3E0' },
  badgeEstado: { backgroundColor: '#E8F5E9' },
  badgeTexto: { fontSize: 11, fontWeight: '700', color: '#1B3A6B' },
  vacio: { textAlign: 'center', color: '#718096', marginTop: 60, fontSize: 15 },
  fab: { position: 'absolute', bottom: 24, right: 24, width: 56, height: 56, borderRadius: 28, backgroundColor: '#1B3A6B', justifyContent: 'center', alignItems: 'center', elevation: 8 },
  fabTexto: { color: '#FFD700', fontSize: 28, fontWeight: '300', lineHeight: 32 },
});
