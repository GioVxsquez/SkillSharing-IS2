import React, { useEffect, useState } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, RefreshControl, Alert, StatusBar,
} from 'react-native';
import { api } from '../api/config';

// US12, US13
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
    } catch {}
  };

  const marcarTodasComoVistas = async () => {
    try {
      await api.put('/notificaciones/marcar-todas');
      setNotificaciones((prev) => prev.map((n) => ({ ...n, visto: true })));
    } catch {
      Alert.alert('Error', 'No se pudieron marcar todas como leídas.');
    }
  };

  const noLeidas = notificaciones.filter((n) => !n.visto).length;

  const renderNotificacion = ({ item }: any) => (
    <TouchableOpacity
      style={[styles.card, !item.visto && styles.cardNoLeida]}
      onPress={() => marcarComoVista(item.notificacionId)}
      activeOpacity={0.8}
    >
      <View style={styles.cardLeft}>
        <View style={[styles.indicador, item.visto && styles.indicadorVisto]} />
      </View>
      <View style={styles.cardBody}>
        <Text style={[styles.cardMensaje, !item.visto && styles.mensajeNoLeido]}>
          {item.mensaje}
        </Text>
        <Text style={styles.cardFecha}>
          {item.fechaCreacion ? new Date(item.fechaCreacion).toLocaleString('es-PE') : 'Hace un momento'}
        </Text>
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0F1C36" />

      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>Volver</Text>
        </TouchableOpacity>
        <View style={styles.headerCenter}>
          <Text style={styles.headerTitulo}>Notificaciones</Text>
          {noLeidas > 0 && (
            <View style={styles.cuentaBadge}>
              <Text style={styles.cuentaBadgeTexto}>{noLeidas}</Text>
            </View>
          )}
        </View>
        {noLeidas > 0 ? (
          <TouchableOpacity onPress={marcarTodasComoVistas}>
            <Text style={styles.marcarBtn}>Leer todo</Text>
          </TouchableOpacity>
        ) : (
          <View style={{ width: 60 }} />
        )}
      </View>

      {loading ? (
        <ActivityIndicator size="large" color="#F97316" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={notificaciones}
          keyExtractor={(item) => String(item.notificacionId)}
          renderItem={renderNotificacion}
          contentContainerStyle={styles.lista}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={() => { setRefreshing(true); cargarNotificaciones(); }}
              colors={['#F97316']}
            />
          }
          ListEmptyComponent={
            <Text style={styles.vacio}>Sin notificaciones por ahora.</Text>
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
  headerCenter: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  headerTitulo: { color: '#FFFFFF', fontSize: 18, fontWeight: '800' },
  cuentaBadge: {
    backgroundColor: '#F97316', borderRadius: 12,
    paddingHorizontal: 8, paddingVertical: 2,
  },
  cuentaBadgeTexto: { color: '#FFFFFF', fontSize: 11, fontWeight: '800' },
  marcarBtn: { color: '#F97316', fontSize: 13, fontWeight: '700' },
  lista: { padding: 16 },
  card: {
    backgroundColor: '#FFFFFF', borderRadius: 14, padding: 16, marginBottom: 10,
    flexDirection: 'row', alignItems: 'flex-start', gap: 12,
    shadowColor: '#0F1C36', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.05, shadowRadius: 6, elevation: 2,
  },
  cardNoLeida: {
    backgroundColor: '#FFF8F4',
    borderLeftWidth: 0,
    borderWidth: 1,
    borderColor: '#FDCBA4',
  },
  cardLeft: { paddingTop: 4 },
  indicador: { width: 8, height: 8, borderRadius: 4, backgroundColor: '#F97316' },
  indicadorVisto: { backgroundColor: '#E2E8F0' },
  cardBody: { flex: 1 },
  cardMensaje: { fontSize: 14, color: '#4A5568', lineHeight: 20, marginBottom: 6 },
  mensajeNoLeido: { fontWeight: '700', color: '#0F1C36' },
  cardFecha: { fontSize: 11, color: '#8898AA', fontWeight: '600' },
  vacio: { textAlign: 'center', color: '#8898AA', marginTop: 60, fontSize: 15 },
});
