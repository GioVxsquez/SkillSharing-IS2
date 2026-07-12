import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, Alert, StatusBar, RefreshControl,
} from 'react-native';
import { api } from '../api/config';

// HU28, HU07
export default function InvitacionesScreen({ navigation }: any) {
  const [invitaciones, setInvitaciones] = useState<any[]>([]);
  const [loading, setLoading]           = useState(true);
  const [refreshing, setRefreshing]     = useState(false);
  const [procesando, setProcesando]     = useState<number | null>(null);

  const cargar = useCallback(async () => {
    try {
      const resp = await api.get('/invitaciones/mis-invitaciones');
      setInvitaciones(resp.data.data || []);
    } catch {
      Alert.alert('Error', 'No se pudieron cargar las invitaciones.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);

  const responder = async (invitacionId: number, aceptar: boolean) => {
    setProcesando(invitacionId);
    try {
      const resp = await api.put(`/invitaciones/${invitacionId}/responder?aceptar=${aceptar}`);
      if (resp.data.ok) {
        Alert.alert('Listo', aceptar ? 'Invitación aceptada.' : 'Invitación rechazada.');
        cargar();
      }
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.mensaje || 'No se pudo procesar la respuesta.');
    } finally {
      setProcesando(null);
    }
  };

  const estadoColor: Record<string, string> = {
    PENDIENTE: '#F59E0B',
    ACEPTADA: '#22C55E',
    RECHAZADA: '#EF4444',
  };

  const renderItem = ({ item }: any) => {
    const estaRespondida = item.estado !== 'PENDIENTE';
    return (
      <View style={styles.card}>
        <View style={styles.cardTop}>
          <Text style={styles.cardTitulo} numberOfLines={2}>{item.sesionTitulo || 'Sesión privada'}</Text>
          <View style={[styles.estadoPill, { backgroundColor: (estadoColor[item.estado] || '#8898AA') + '22' }]}>
            <View style={[styles.estadoPunto, { backgroundColor: estadoColor[item.estado] || '#8898AA' }]} />
            <Text style={[styles.estadoTexto, { color: estadoColor[item.estado] || '#8898AA' }]}>{item.estado}</Text>
          </View>
        </View>
        <Text style={styles.cardMeta}>
          {item.emisorNombre || 'Instructor'} · {item.fechaSesion ? new Date(item.fechaSesion).toLocaleDateString('es-PE', { day: '2-digit', month: 'short' }) : '—'}
        </Text>
        {!estaRespondida && (
          <View style={styles.botones}>
            <TouchableOpacity
              style={[styles.botonAceptar, procesando === item.invitacionId && { opacity: 0.6 }]}
              onPress={() => responder(item.invitacionId, true)}
              disabled={procesando === item.invitacionId}
            >
              {procesando === item.invitacionId
                ? <ActivityIndicator size="small" color="#fff" />
                : <Text style={styles.botonAceptarTexto}>Aceptar</Text>}
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.botonRechazar, procesando === item.invitacionId && { opacity: 0.6 }]}
              onPress={() => responder(item.invitacionId, false)}
              disabled={procesando === item.invitacionId}
            >
              <Text style={styles.botonRechazarTexto}>Rechazar</Text>
            </TouchableOpacity>
          </View>
        )}
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0F1C36" />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Invitaciones</Text>
        <View style={{ width: 40 }} />
      </View>

      {loading ? (
        <ActivityIndicator size="large" color="#F97316" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={invitaciones}
          keyExtractor={(item) => String(item.invitacionId)}
          renderItem={renderItem}
          contentContainerStyle={styles.lista}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); cargar(); }} colors={['#F97316']} />}
          ListEmptyComponent={<Text style={styles.vacio}>Sin invitaciones por ahora.</Text>}
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
  lista: { padding: 16, paddingBottom: 40 },
  card: {
    backgroundColor: '#FFFFFF', borderRadius: 16, padding: 18, marginBottom: 12,
    shadowColor: '#0F1C36', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.07, shadowRadius: 8, elevation: 3,
  },
  cardTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8, gap: 10 },
  cardTitulo: { fontSize: 15, fontWeight: '800', color: '#0F1C36', flex: 1, lineHeight: 21 },
  estadoPill: {
    flexDirection: 'row', alignItems: 'center', gap: 5,
    paddingHorizontal: 10, paddingVertical: 4, borderRadius: 20, flexShrink: 0,
  },
  estadoPunto: { width: 6, height: 6, borderRadius: 3 },
  estadoTexto: { fontSize: 11, fontWeight: '800' },
  cardMeta: { fontSize: 13, color: '#8898AA', fontWeight: '600', marginBottom: 14 },
  botones: { flexDirection: 'row', gap: 10 },
  botonAceptar: {
    flex: 1, backgroundColor: '#F97316', paddingVertical: 12,
    borderRadius: 12, alignItems: 'center',
    shadowColor: '#F97316', shadowOffset: { width: 0, height: 3 }, shadowOpacity: 0.3, shadowRadius: 6, elevation: 4,
  },
  botonAceptarTexto: { color: '#FFFFFF', fontWeight: '800', fontSize: 14 },
  botonRechazar: {
    flex: 1, borderWidth: 1.5, borderColor: '#EF4444',
    paddingVertical: 12, borderRadius: 12, alignItems: 'center',
    backgroundColor: '#FFF5F5',
  },
  botonRechazarTexto: { color: '#EF4444', fontWeight: '800', fontSize: 14 },
  vacio: { textAlign: 'center', color: '#8898AA', marginTop: 60, fontSize: 15 },
});
