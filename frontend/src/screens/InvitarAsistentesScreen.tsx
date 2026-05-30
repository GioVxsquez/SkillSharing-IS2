import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, Alert, StatusBar, TextInput, Modal,
} from 'react-native';
import { api } from '../api/config';

// HU06: Invitar asistentes a una sesión privada (solo instructores)
export default function InvitarAsistentesScreen({ route, navigation }: any) {
  const { sesionId, sesionTitulo } = route.params;
  const [busqueda, setBusqueda]   = useState('');
  const [usuarios, setUsuarios]   = useState<any[]>([]);
  const [loading, setLoading]     = useState(false);
  const [invitando, setInvitando] = useState<number | null>(null);

  const buscarUsuarios = useCallback(async () => {
    if (busqueda.trim().length < 2) return;
    setLoading(true);
    try {
      const resp = await api.get(`/usuarios/buscar?q=${encodeURIComponent(busqueda.trim())}`);
      setUsuarios(resp.data.data || []);
    } catch {
      Alert.alert('Error', 'No se pudo realizar la búsqueda.');
    } finally {
      setLoading(false);
    }
  }, [busqueda]);

  const invitar = async (usuarioId: number, nombre: string) => {
    setInvitando(usuarioId);
    try {
      const resp = await api.post('/invitaciones', {
        sesionId,
        receptorId: usuarioId,
      });
      if (resp.data.ok) {
        Alert.alert('✅ Invitación enviada', `Se invitó a ${nombre} a la sesión.`);
      } else {
        Alert.alert('Aviso', resp.data.mensaje || 'No se pudo enviar la invitación.');
      }
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.mensaje || 'Error al enviar la invitación.');
    } finally {
      setInvitando(null);
    }
  };

  const renderUsuario = ({ item }: any) => (
    <View style={styles.card}>
      <View style={styles.cardAvatar}>
        <Text style={styles.avatarLetra}>{item.nombre?.charAt(0)?.toUpperCase()}</Text>
      </View>
      <View style={styles.cardInfo}>
        <Text style={styles.cardNombre}>{item.nombre}</Text>
        <Text style={styles.cardEmail}>{item.email}</Text>
        <Text style={styles.cardRol}>{item.rol === 'INSTRUCTOR' ? '📚 Instructor' : '🎓 Aprendiz'}</Text>
      </View>
      <TouchableOpacity
        style={[styles.botonInvitar, invitando === item.usuarioId && { opacity: 0.6 }]}
        onPress={() => invitar(item.usuarioId, item.nombre)}
        disabled={invitando === item.usuarioId}
      >
        {invitando === item.usuarioId
          ? <ActivityIndicator size="small" color="#fff" />
          : <Text style={styles.botonTexto}>Invitar</Text>}
      </TouchableOpacity>
    </View>
  );

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1B3A6B" />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>← Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Invitar Asistentes</Text>
        <Text style={styles.headerSub} numberOfLines={1}>{sesionTitulo}</Text>
      </View>

      <View style={styles.busquedaContainer}>
        <TextInput
          style={styles.busquedaInput}
          placeholder="Buscar por nombre o correo..."
          placeholderTextColor="#B0B8C1"
          value={busqueda}
          onChangeText={setBusqueda}
          onSubmitEditing={buscarUsuarios}
          returnKeyType="search"
        />
        <TouchableOpacity style={styles.botonBuscar} onPress={buscarUsuarios}>
          <Text style={styles.botonBuscarTexto}>🔍</Text>
        </TouchableOpacity>
      </View>

      {loading ? (
        <ActivityIndicator size="large" color="#1B3A6B" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={usuarios}
          keyExtractor={(item) => String(item.usuarioId)}
          renderItem={renderUsuario}
          contentContainerStyle={styles.lista}
          ListEmptyComponent={
            <Text style={styles.vacio}>
              {busqueda.length >= 2 ? 'No se encontraron usuarios.' : 'Escribe al menos 2 caracteres para buscar.'}
            </Text>
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F9FC' },
  header: { backgroundColor: '#1B3A6B', paddingTop: 48, paddingBottom: 16, paddingHorizontal: 20 },
  backTexto: { color: '#C8D8F0', fontSize: 14, marginBottom: 4 },
  headerTitulo: { color: '#FFFFFF', fontSize: 20, fontWeight: '700' },
  headerSub: { color: '#C8D8F0', fontSize: 13, marginTop: 2 },
  busquedaContainer: { flexDirection: 'row', margin: 16, gap: 10 },
  busquedaInput: { flex: 1, backgroundColor: '#FFFFFF', borderWidth: 1.5, borderColor: '#E2E8F0', borderRadius: 10, paddingVertical: 12, paddingHorizontal: 16, fontSize: 15, color: '#1A202C' },
  botonBuscar: { backgroundColor: '#1B3A6B', width: 48, borderRadius: 10, justifyContent: 'center', alignItems: 'center' },
  botonBuscarTexto: { fontSize: 20 },
  lista: { paddingHorizontal: 16, paddingBottom: 40 },
  card: { backgroundColor: '#FFFFFF', borderRadius: 12, padding: 14, marginBottom: 10, flexDirection: 'row', alignItems: 'center', shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.06, shadowRadius: 4, elevation: 2 },
  cardAvatar: { width: 46, height: 46, borderRadius: 23, backgroundColor: '#1B3A6B', justifyContent: 'center', alignItems: 'center', marginRight: 12 },
  avatarLetra: { color: '#FFD700', fontSize: 20, fontWeight: '700' },
  cardInfo: { flex: 1 },
  cardNombre: { fontSize: 15, fontWeight: '700', color: '#1A202C' },
  cardEmail: { fontSize: 12, color: '#718096', marginTop: 2 },
  cardRol: { fontSize: 12, color: '#4A5568', marginTop: 2 },
  botonInvitar: { backgroundColor: '#1B3A6B', paddingHorizontal: 14, paddingVertical: 9, borderRadius: 8 },
  botonTexto: { color: '#FFFFFF', fontWeight: '700', fontSize: 13 },
  vacio: { textAlign: 'center', color: '#718096', marginTop: 50, fontSize: 15 },
});
