import React, { useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, Alert, StatusBar, TextInput,
} from 'react-native';
import { api } from '../api/config';

// HU06
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
      const resp = await api.post('/invitaciones', { sesionId, receptorId: usuarioId });
      if (resp.data.ok) {
        Alert.alert('Listo', `${nombre} fue invitado a la sesión.`);
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
      <View style={styles.avatar}>
        <Text style={styles.avatarLetra}>{item.nombre?.charAt(0)?.toUpperCase()}</Text>
      </View>
      <View style={styles.cardInfo}>
        <Text style={styles.cardNombre}>{item.nombre}</Text>
        <Text style={styles.cardEmail}>{item.email}</Text>
        <Text style={styles.cardRol}>{item.rol === 'INSTRUCTOR' ? 'Instructor' : 'Aprendiz'}</Text>
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
      <StatusBar barStyle="light-content" backgroundColor="#0F1C36" />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>Volver</Text>
        </TouchableOpacity>
        <View style={styles.headerTextos}>
          <Text style={styles.headerTitulo}>Invitar personas</Text>
          <Text style={styles.headerSub} numberOfLines={1}>{sesionTitulo}</Text>
        </View>
      </View>

      <View style={styles.busquedaWrap}>
        <TextInput
          style={styles.busquedaInput}
          placeholder="Buscar por nombre o correo..."
          placeholderTextColor="#8898AA"
          value={busqueda}
          onChangeText={setBusqueda}
          onSubmitEditing={buscarUsuarios}
          returnKeyType="search"
        />
        <TouchableOpacity style={styles.botonBuscar} onPress={buscarUsuarios}>
          <Text style={styles.botonBuscarTexto}>Buscar</Text>
        </TouchableOpacity>
      </View>

      {loading ? (
        <ActivityIndicator size="large" color="#F97316" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={usuarios}
          keyExtractor={(item) => String(item.usuarioId)}
          renderItem={renderUsuario}
          contentContainerStyle={styles.lista}
          ListEmptyComponent={
            <Text style={styles.vacio}>
              {busqueda.length >= 2 ? 'No se encontraron usuarios.' : 'Escribe un nombre o correo para buscar.'}
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
    flexDirection: 'row', alignItems: 'flex-end', gap: 16,
  },
  backTexto: { color: '#8898AA', fontSize: 14, fontWeight: '600', marginBottom: 2 },
  headerTextos: { flex: 1 },
  headerTitulo: { color: '#FFFFFF', fontSize: 18, fontWeight: '800' },
  headerSub: { color: '#8898AA', fontSize: 12, marginTop: 2 },
  busquedaWrap: {
    flexDirection: 'row', gap: 10,
    paddingHorizontal: 16, paddingVertical: 14,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1, borderBottomColor: '#E8EDF2',
  },
  busquedaInput: {
    flex: 1, backgroundColor: '#F0F4F8',
    borderWidth: 1.5, borderColor: '#E2E8F0',
    borderRadius: 12, paddingVertical: 12, paddingHorizontal: 14,
    fontSize: 14, color: '#0F1C36',
  },
  botonBuscar: {
    backgroundColor: '#F97316', borderRadius: 12,
    paddingHorizontal: 16, justifyContent: 'center', alignItems: 'center',
  },
  botonBuscarTexto: { color: '#FFFFFF', fontWeight: '800', fontSize: 13 },
  lista: { paddingHorizontal: 16, paddingTop: 12, paddingBottom: 40 },
  card: {
    backgroundColor: '#FFFFFF', borderRadius: 14, padding: 14, marginBottom: 10,
    flexDirection: 'row', alignItems: 'center', gap: 12,
    shadowColor: '#0F1C36', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.06, shadowRadius: 6, elevation: 2,
  },
  avatar: {
    width: 46, height: 46, borderRadius: 23,
    backgroundColor: '#0F1C36', justifyContent: 'center', alignItems: 'center', flexShrink: 0,
  },
  avatarLetra: { color: '#F97316', fontSize: 20, fontWeight: '900' },
  cardInfo: { flex: 1 },
  cardNombre: { fontSize: 14, fontWeight: '800', color: '#0F1C36' },
  cardEmail: { fontSize: 12, color: '#8898AA', marginTop: 2 },
  cardRol: { fontSize: 11, color: '#4A5568', marginTop: 2, fontWeight: '600' },
  botonInvitar: {
    backgroundColor: '#0F1C36', paddingHorizontal: 14, paddingVertical: 9, borderRadius: 10,
  },
  botonTexto: { color: '#FFFFFF', fontWeight: '800', fontSize: 12 },
  vacio: { textAlign: 'center', color: '#8898AA', marginTop: 50, fontSize: 15 },
});
