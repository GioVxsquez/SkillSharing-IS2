import React, { useEffect, useState } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, RefreshControl, Alert, StatusBar,
} from 'react-native';
import { api } from '../api/config';

// US12, US13: Pantalla de notificaciones
export default function NotificacionesScreen({ navigation }: any) {
  const [notificaciones, setNotificaciones] = useState<any[]>([]);
  const [loading, setLoading]               = useState(true);
  const [refreshing, setRefreshing]         = useState(false);

  const cargarNotificaciones = async () => {
    try {
      const resp = await api.get('/notificaciones');
      setNotificaciones(resp.data.data || []);
    } catch {
      Alert.alert('Error', 'No se pudieron cargar las notificaciones.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    cargarNotificaciones();
  }, []);

  const marcarComoVista = async (id: number) => {
    try {
      await api.put(`/notificaciones/${id}/ver`);
      setNotificaciones((prev) =>
        prev.map((n) => (n.notificacionId === id ? { ...n, visto: true } : n))
      );
    } catch {
      // Ignorar si falla
    }
  };

  const marcarTodasComoVistas = async () => {
    try {
      await api.put('/notificaciones/marcar-todas');
      setNotificaciones((prev) => prev.map((n) => ({ ...n, visto: true })));
      Alert.alert('Éxito', 'Todas las notificaciones fueron marcadas como leídas.');
    } catch {
      Alert.alert('Error', 'No se pudieron marcar todas como leídas.');
    }
  };

  const renderNotificacion = ({ item }: any) => (
    <TouchableOpacity
      style={[styles.card, !item.visto && styles.cardNoLeida]}
      onPress={() => marcarComoVista(item.notificacionId)}
      activeOpacity={0.8}
    >
      <View style={styles.cardHeader}>
        <Text style={[styles.cardMensaje, !item.visto && styles.mensajeNoLeido]}>
          {item.mensaje}
        </Text>
        {!item.visto && <View style={styles.dot} />}
      </View>
      <Text style={styles.cardFecha}>
        📅 {item.fechaCreacion ? new Date(item.fechaCreacion).toLocaleString('es-PE') : 'Hace un momento'}
      </Text>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1B3A6B" />

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>← Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Notificaciones</Text>
        {notificaciones.some((n) => !n.visto) ? (
          <TouchableOpacity onPress={marcarTodasComoVistas}>
            <Text style={styles.marcarLeidoBtn}>Marcar todo leído</Text>
          </TouchableOpacity>
        ) : (
          <View style={{ width: 80 }} />
        )}
      </View>

      {loading ? (
        <ActivityIndicator size="large" color="#1B3A6B" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={notificaciones}
          keyExtractor={(item) => String(item.notificacionId)}
          renderItem={renderNotificacion}
          contentContainerStyle={styles.lista}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={() => {
                setRefreshing(true);
                cargarNotificaciones();
              }}
              colors={['#1B3A6B']}
            />
          }
          ListEmptyComponent={
            <Text style={styles.vacio}>No tienes notificaciones pendientes.</Text>
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F9FC' },
  header: {
    backgroundColor: '#1B3A6B',
    paddingTop: 48,
    paddingBottom: 16,
    paddingHorizontal: 16,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
  },
  backTexto: { color: '#C8D8F0', fontSize: 14, fontWeight: '600' },
  headerTitulo: { color: '#FFFFFF', fontSize: 18, fontWeight: '700' },
  marcarLeidoBtn: { color: '#FFD700', fontSize: 12, fontWeight: '600' },
  lista: { padding: 16 },
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    shadowColor: '#1B3A6B',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 3,
    elevation: 2,
  },
  cardNoLeida: {
    backgroundColor: '#E6F4FF',
    borderColor: '#91CAFF',
  },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  cardMensaje: { fontSize: 14, color: '#4A5568', flex: 1, lineHeight: 20 },
  mensajeNoLeido: { fontWeight: '700', color: '#1A202C' },
  dot: { width: 8, height: 8, borderRadius: 4, backgroundColor: '#1B3A6B', marginLeft: 8, marginTop: 6 },
  cardFecha: { fontSize: 11, color: '#718096', marginTop: 8 },
  vacio: { textAlign: 'center', color: '#718096', marginTop: 60, fontSize: 15 },
});
