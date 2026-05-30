import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, RefreshControl, StatusBar, Alert,
} from 'react-native';
import { api } from '../api/config';
import AsyncStorage from '@react-native-async-storage/async-storage';

// HU16: Visualizar eventos públicos (sesiones activas disponibles para todos)
export default function HomeScreen({ navigation }: any) {
  const [sesiones, setSesiones]     = useState<any[]>([]);
  const [loading, setLoading]       = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const cargarSesiones = useCallback(async () => {
    try {
      const resp = await api.get('/sesiones/publicas');
      setSesiones(resp.data.data || []);
    } catch {
      Alert.alert('Error', 'No se pudieron cargar las sesiones.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { cargarSesiones(); }, [cargarSesiones]);

  const handleLogout = async () => {
    await AsyncStorage.removeItem('userToken');
    navigation.replace('Login');
  };

  const renderSesion = ({ item }: any) => (
    <TouchableOpacity
      style={styles.card}
      onPress={() => navigation.navigate('DetalleSesion', { sesionId: item.sesionId })}
      activeOpacity={0.8}
    >
      <View style={styles.cardHeader}>
        <Text style={styles.cardTitulo} numberOfLines={2}>{item.titulo}</Text>
        <View style={[styles.badge, item.tipo === 'PUBLICA' ? styles.badgePublica : styles.badgePrivada]}>
          <Text style={styles.badgeTexto}>{item.tipo}</Text>
        </View>
      </View>
      <Text style={styles.cardInstructor}>👤 {item.instructorNombre || 'Instructor'}</Text>
      <Text style={styles.cardFecha}>📅 {item.fechaSesion ? new Date(item.fechaSesion).toLocaleDateString('es-PE') : 'Por confirmar'}</Text>
      {item.descripcion ? (
        <Text style={styles.cardDesc} numberOfLines={2}>{item.descripcion}</Text>
      ) : null}
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1B3A6B" />

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitulo}>SkillSharing</Text>
        <View style={styles.headerAcciones}>
          <TouchableOpacity onPress={() => navigation.navigate('MisSesiones')} style={styles.headerBtn}>
            <Text style={styles.headerBtnTexto}>Mis Sesiones</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={() => navigation.navigate('Perfil')} style={styles.headerBtn}>
            <Text style={styles.headerBtnTexto}>Perfil</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={handleLogout}>
            <Text style={[styles.headerBtnTexto, { color: '#FFD700' }]}>Salir</Text>
          </TouchableOpacity>
        </View>
      </View>

      <Text style={styles.subtitulo}>Sesiones disponibles</Text>

      {loading ? (
        <ActivityIndicator size="large" color="#1B3A6B" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={sesiones}
          keyExtractor={(item) => String(item.sesionId)}
          renderItem={renderSesion}
          contentContainerStyle={styles.lista}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); cargarSesiones(); }} colors={['#1B3A6B']} />}
          ListEmptyComponent={<Text style={styles.vacio}>No hay sesiones disponibles aún.</Text>}
        />
      )}

      {/* FAB - Crear sesión (solo instructores lo usarán, pero visible para demo) */}
      <TouchableOpacity style={styles.fab} onPress={() => navigation.navigate('CrearSesion')}>
        <Text style={styles.fabTexto}>+</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F9FC' },
  header: { backgroundColor: '#1B3A6B', paddingTop: 48, paddingBottom: 16, paddingHorizontal: 20, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-end' },
  headerTitulo: { color: '#FFD700', fontSize: 22, fontWeight: '800', letterSpacing: 1 },
  headerAcciones: { flexDirection: 'row', gap: 14 },
  headerBtn: {},
  headerBtnTexto: { color: '#C8D8F0', fontSize: 13, fontWeight: '600' },
  subtitulo: { fontSize: 16, fontWeight: '700', color: '#1B3A6B', margin: 16 },
  lista: { paddingHorizontal: 16, paddingBottom: 80 },
  card: { backgroundColor: '#FFFFFF', borderRadius: 14, padding: 16, marginBottom: 14, shadowColor: '#1B3A6B', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.08, shadowRadius: 6, elevation: 3 },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 },
  cardTitulo: { fontSize: 16, fontWeight: '700', color: '#1A202C', flex: 1, marginRight: 8 },
  badge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 20 },
  badgePublica: { backgroundColor: '#E6F4FF' },
  badgePrivada: { backgroundColor: '#FFF3E0' },
  badgeTexto: { fontSize: 11, fontWeight: '700', color: '#1B3A6B' },
  cardInstructor: { fontSize: 13, color: '#4A5568', marginBottom: 4 },
  cardFecha: { fontSize: 13, color: '#4A5568', marginBottom: 6 },
  cardDesc: { fontSize: 13, color: '#718096', lineHeight: 18 },
  vacio: { textAlign: 'center', color: '#718096', marginTop: 60, fontSize: 15 },
  fab: { position: 'absolute', bottom: 24, right: 24, width: 56, height: 56, borderRadius: 28, backgroundColor: '#1B3A6B', justifyContent: 'center', alignItems: 'center', shadowColor: '#1B3A6B', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.3, shadowRadius: 8, elevation: 8 },
  fabTexto: { color: '#FFD700', fontSize: 28, fontWeight: '300', lineHeight: 32 },
});
