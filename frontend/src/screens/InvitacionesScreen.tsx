import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, Alert, StatusBar, RefreshControl,
} from 'react-native';
import { api } from '../api/config';

// HU28: Visualizar invitaciones privadas recibidas
// HU07: Confirmar/Rechazar asistencia a sesión privada
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

  // HU07: aceptar o rechazar invitación privada
  const responder = async (invitacionId: number, aceptar: boolean) => {
    setProcesando(invitacionId);
    try {
      const resp = await api.put(`/invitaciones/${invitacionId}/responder?aceptar=${aceptar}`);
      if (resp.data.exito) {
        Alert.alert('¡Listo!', aceptar ? 'Invitación aceptada.' : 'Invitación rechazada.');
        cargar();
      }
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.mensaje || 'No se pudo procesar la respuesta.');
    } finally {
      setProcesando(null);
    }
  };

  const renderItem = ({ item }: any) => {
    const estaRespondida = item.estado !== 'PENDIENTE';
    return (
      <View style={styles.card}>
        <Text style={styles.cardTitulo}>{item.sesionTitulo || 'Sesión privada'}</Text>
        <Text style={styles.cardInfo}>👤 De: {item.emisorNombre || 'Instructor'}</Text>
        <Text style={styles.cardInfo}>📅 {item.fechaSesion ? new Date(item.fechaSesion).toLocaleDateString('es-PE') : '—'}</Text>

        <View style={[styles.estadoBadge, estaRespondida ? styles.estadoRespondida : styles.estadoPendiente]}>
          <Text style={styles.estadoTexto}>{item.estado}</Text>
        </View>

        {!estaRespondida && (
          <View style={styles.botones}>
            <TouchableOpacity
              style={[styles.botonAceptar, procesando === item.invitacionId && { opacity: 0.5 }]}
              onPress={() => responder(item.invitacionId, true)}
              disabled={procesando === item.invitacionId}
            >
              {procesando === item.invitacionId
                ? <ActivityIndicator size="small" color="#fff" />
                : <Text style={styles.botonTexto}>✅ Aceptar</Text>}
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.botonRechazar, procesando === item.invitacionId && { opacity: 0.5 }]}
              onPress={() => responder(item.invitacionId, false)}
              disabled={procesando === item.invitacionId}
            >
              <Text style={styles.botonTextoRechazo}>❌ Rechazar</Text>
            </TouchableOpacity>
          </View>
        )}
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1B3A6B" />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>← Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Mis Invitaciones</Text>
      </View>

      {loading ? (
        <ActivityIndicator size="large" color="#1B3A6B" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={invitaciones}
          keyExtractor={(item) => String(item.invitacionId)}
          renderItem={renderItem}
          contentContainerStyle={styles.lista}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); cargar(); }} colors={['#1B3A6B']} />}
          ListEmptyComponent={<Text style={styles.vacio}>No tienes invitaciones pendientes.</Text>}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F9FC' },
  header: { backgroundColor: '#1B3A6B', paddingTop: 48, paddingBottom: 16, paddingHorizontal: 20 },
  backTexto: { color: '#C8D8F0', fontSize: 14, marginBottom: 6 },
  headerTitulo: { color: '#FFFFFF', fontSize: 20, fontWeight: '700' },
  lista: { padding: 16, paddingBottom: 40 },
  card: { backgroundColor: '#FFFFFF', borderRadius: 14, padding: 16, marginBottom: 14, shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.06, shadowRadius: 6, elevation: 2 },
  cardTitulo: { fontSize: 16, fontWeight: '700', color: '#1A202C', marginBottom: 8 },
  cardInfo: { fontSize: 13, color: '#4A5568', marginBottom: 4 },
  estadoBadge: { alignSelf: 'flex-start', paddingHorizontal: 12, paddingVertical: 4, borderRadius: 20, marginTop: 10 },
  estadoPendiente: { backgroundColor: '#FFF3E0' },
  estadoRespondida: { backgroundColor: '#E8F5E9' },
  estadoTexto: { fontSize: 12, fontWeight: '700', color: '#1B3A6B' },
  botones: { flexDirection: 'row', gap: 10, marginTop: 14 },
  botonAceptar: { flex: 1, backgroundColor: '#1B3A6B', paddingVertical: 12, borderRadius: 10, alignItems: 'center' },
  botonRechazar: { flex: 1, borderWidth: 2, borderColor: '#E53E3E', paddingVertical: 12, borderRadius: 10, alignItems: 'center' },
  botonTexto: { color: '#FFFFFF', fontWeight: '700', fontSize: 14 },
  botonTextoRechazo: { color: '#E53E3E', fontWeight: '700', fontSize: 14 },
  vacio: { textAlign: 'center', color: '#718096', marginTop: 60, fontSize: 15 },
});
