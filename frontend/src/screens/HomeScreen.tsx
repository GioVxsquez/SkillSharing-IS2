import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, RefreshControl, StatusBar, Alert, TextInput, ScrollView
} from 'react-native';
import { api } from '../api/config';
import AsyncStorage from '@react-native-async-storage/async-storage';

// US03, US09, US11: Visualizar eventos públicos con buscador y filtros
export default function HomeScreen({ navigation }: any) {
  const [sesiones, setSesiones]     = useState<any[]>([]);
  const [loading, setLoading]       = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [selectedModalidad, setSelectedModalidad] = useState<string | null>(null);
  const [unreadNotifications, setUnreadNotifications] = useState(0);

  const categorias = ['Programacion', 'Idiomas', 'Cocina', 'Diseno Grafico', 'Matematicas', 'Musica', 'Fotografia', 'Marketing Digital', 'Finanzas'];

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

  const cargarContadorNotificaciones = useCallback(async () => {
    try {
      const resp = await api.get('/notificaciones/no-vistas/count');
      setUnreadNotifications(resp.data.data?.total || 0);
    } catch {
      // Ignorar silenciosamente si falla el contador
    }
  }, []);

  useEffect(() => {
    cargarSesiones();
    cargarContadorNotificaciones();
  }, [cargarSesiones, cargarContadorNotificaciones]);

  // Recargar al regresar a la pantalla
  useEffect(() => {
    const unsubscribe = navigation.addListener('focus', () => {
      cargarSesiones();
      cargarContadorNotificaciones();
      setSearchQuery('');
      setSelectedCategory(null);
      setSelectedModalidad(null);
    });
    return unsubscribe;
  }, [navigation, cargarSesiones, cargarContadorNotificaciones]);

  const handleLogout = async () => {
    await AsyncStorage.removeItem('userToken');
    navigation.replace('Login');
  };

  const buscarSesiones = async (query: string) => {
    setSearchQuery(query);
    setSelectedCategory(null);
    setSelectedModalidad(null);
    if (query.trim() === '') {
      cargarSesiones();
      return;
    }
    try {
      setLoading(true);
      const resp = await api.get(`/sesiones/buscar?q=${query}`);
      setSesiones(resp.data.data || []);
    } catch {
      Alert.alert('Error', 'No se pudo realizar la búsqueda.');
    } finally {
      setLoading(false);
    }
  };

  const filtrarCategoria = async (cat: string | null) => {
    setSelectedCategory(cat);
    setSelectedModalidad(null);
    setSearchQuery('');
    if (!cat) {
      cargarSesiones();
      return;
    }
    try {
      setLoading(true);
      const resp = await api.get(`/sesiones/categoria/${cat}`);
      setSesiones(resp.data.data || []);
    } catch {
      Alert.alert('Error', 'No se pudieron filtrar las sesiones.');
    } finally {
      setLoading(false);
    }
  };

  const filtrarModalidad = async (mod: string | null) => {
    setSelectedModalidad(mod);
    setSelectedCategory(null);
    setSearchQuery('');
    if (!mod) {
      cargarSesiones();
      return;
    }
    try {
      setLoading(true);
      const resp = await api.get(`/sesiones/modalidad/${mod}`);
      setSesiones(resp.data.data || []);
    } catch {
      Alert.alert('Error', 'No se pudieron filtrar las sesiones.');
    } finally {
      setLoading(false);
    }
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
      <View style={styles.row}>
        <Text style={styles.cardFecha}>📅 {item.fechaSesion ? new Date(item.fechaSesion).toLocaleDateString('es-PE') : 'Por confirmar'}</Text>
        <View style={[styles.modalidadBadge, item.modalidad === 'VIRTUAL' ? styles.badgeVirtual : styles.badgePresencial]}>
          <Text style={styles.modalidadTexto}>{item.modalidad}</Text>
        </View>
      </View>
      {item.categoria ? (
        <Text style={styles.cardCategoria}>🏷️ Categoría: {item.categoria}</Text>
      ) : null}
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
          <TouchableOpacity onPress={() => navigation.navigate('Notificaciones')} style={styles.headerBtn}>
            <Text style={styles.headerBtnTexto}>Alertas {unreadNotifications > 0 ? `(${unreadNotifications})` : ''}</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={() => navigation.navigate('BuscarInstructores')} style={styles.headerBtn}>
            <Text style={styles.headerBtnTexto}>Instructores</Text>
          </TouchableOpacity>
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

      {/* Buscador */}
      <View style={styles.searchContainer}>
        <TextInput
          style={styles.searchInput}
          placeholder="Buscar sesión por título..."
          value={searchQuery}
          onChangeText={buscarSesiones}
          placeholderTextColor="#718096"
        />
      </View>

      {/* Filtros de Modalidad */}
      <View style={styles.filterSection}>
        <Text style={styles.filterLabel}>Modalidad:</Text>
        <View style={styles.row}>
          <TouchableOpacity
            style={[styles.filterChip, selectedModalidad === null && styles.filterChipActive]}
            onPress={() => filtrarModalidad(null)}
          >
            <Text style={[styles.filterChipText, selectedModalidad === null && styles.filterChipTextActive]}>Todas</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.filterChip, selectedModalidad === 'VIRTUAL' && styles.filterChipActive]}
            onPress={() => filtrarModalidad('VIRTUAL')}
          >
            <Text style={[styles.filterChipText, selectedModalidad === 'VIRTUAL' && styles.filterChipTextActive]}>Virtual</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.filterChip, selectedModalidad === 'PRESENCIAL' && styles.filterChipActive]}
            onPress={() => filtrarModalidad('PRESENCIAL')}
          >
            <Text style={[styles.filterChipText, selectedModalidad === 'PRESENCIAL' && styles.filterChipTextActive]}>Presencial</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Filtros de Categorías */}
      <View style={styles.categorySection}>
        <Text style={styles.filterLabel}>Categoría:</Text>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.scrollCategorias}>
          <TouchableOpacity
            style={[styles.categoryChip, selectedCategory === null && styles.categoryChipActive]}
            onPress={() => filtrarCategoria(null)}
          >
            <Text style={[styles.categoryChipText, selectedCategory === null && styles.categoryChipTextActive]}>Todas</Text>
          </TouchableOpacity>
          {categorias.map((cat) => (
            <TouchableOpacity
              key={cat}
              style={[styles.categoryChip, selectedCategory === cat && styles.categoryChipActive]}
              onPress={() => filtrarCategoria(cat)}
            >
              <Text style={[styles.categoryChipText, selectedCategory === cat && styles.categoryChipTextActive]}>{cat}</Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
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
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); cargarSesiones(); cargarContadorNotificaciones(); }} colors={['#1B3A6B']} />}
          ListEmptyComponent={<Text style={styles.vacio}>No hay sesiones disponibles aún.</Text>}
        />
      )}

      {/* FAB - Crear sesión */}
      <TouchableOpacity style={styles.fab} onPress={() => navigation.navigate('CrearSesion')}>
        <Text style={styles.fabTexto}>+</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F9FC' },
  header: { backgroundColor: '#1B3A6B', paddingTop: 48, paddingBottom: 16, paddingHorizontal: 16, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-end' },
  headerTitulo: { color: '#FFD700', fontSize: 20, fontWeight: '800', letterSpacing: 1 },
  headerAcciones: { flexDirection: 'row', gap: 10 },
  headerBtn: {},
  headerBtnTexto: { color: '#C8D8F0', fontSize: 12, fontWeight: '600' },
  subtitulo: { fontSize: 16, fontWeight: '700', color: '#1B3A6B', marginHorizontal: 16, marginTop: 12, marginBottom: 8 },
  searchContainer: { padding: 12, backgroundColor: '#FFFFFF', borderBottomWidth: 1, borderBottomColor: '#E2E8F0' },
  searchInput: { height: 40, backgroundColor: '#F1F5F9', borderRadius: 8, paddingHorizontal: 12, color: '#1A202C' },
  filterSection: { paddingHorizontal: 16, paddingTop: 10, backgroundColor: '#FFFFFF' },
  categorySection: { paddingHorizontal: 16, paddingVertical: 10, backgroundColor: '#FFFFFF', borderBottomWidth: 1, borderBottomColor: '#E2E8F0' },
  filterLabel: { fontSize: 12, fontWeight: '700', color: '#718096', marginBottom: 6 },
  row: { flexDirection: 'row', gap: 8, alignItems: 'center' },
  scrollCategorias: { gap: 8, paddingRight: 16 },
  filterChip: { paddingHorizontal: 12, paddingVertical: 6, borderRadius: 16, backgroundColor: '#F1F5F9' },
  filterChipActive: { backgroundColor: '#1B3A6B' },
  filterChipText: { fontSize: 12, color: '#4A5568', fontWeight: '600' },
  filterChipTextActive: { color: '#FFFFFF' },
  categoryChip: { paddingHorizontal: 12, paddingVertical: 6, borderRadius: 16, backgroundColor: '#F1F5F9' },
  categoryChipActive: { backgroundColor: '#1B3A6B' },
  categoryChipText: { fontSize: 12, color: '#4A5568', fontWeight: '600' },
  categoryChipTextActive: { color: '#FFFFFF' },
  lista: { paddingHorizontal: 16, paddingBottom: 80, paddingTop: 8 },
  card: { backgroundColor: '#FFFFFF', borderRadius: 14, padding: 16, marginBottom: 14, shadowColor: '#1B3A6B', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.08, shadowRadius: 6, elevation: 3 },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 },
  cardTitulo: { fontSize: 16, fontWeight: '700', color: '#1A202C', flex: 1, marginRight: 8 },
  badge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 20 },
  badgePublica: { backgroundColor: '#E6F4FF' },
  badgePrivada: { backgroundColor: '#FFF3E0' },
  badgeTexto: { fontSize: 11, fontWeight: '700', color: '#1B3A6B' },
  cardInstructor: { fontSize: 13, color: '#4A5568', marginBottom: 4 },
  cardFecha: { fontSize: 13, color: '#4A5568' },
  cardCategoria: { fontSize: 12, color: '#1B3A6B', fontWeight: '600', marginTop: 4, marginBottom: 6 },
  modalidadBadge: { paddingHorizontal: 8, paddingVertical: 2, borderRadius: 4 },
  badgeVirtual: { backgroundColor: '#E8F5E9' },
  badgePresencial: { backgroundColor: '#ECEFF1' },
  modalidadTexto: { fontSize: 11, fontWeight: '700', color: '#2E7D32' },
  cardDesc: { fontSize: 13, color: '#718096', lineHeight: 18, marginTop: 4 },
  vacio: { textAlign: 'center', color: '#718096', marginTop: 60, fontSize: 15 },
  fab: { position: 'absolute', bottom: 24, right: 24, width: 56, height: 56, borderRadius: 28, backgroundColor: '#1B3A6B', justifyContent: 'center', alignItems: 'center', shadowColor: '#1B3A6B', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.3, shadowRadius: 8, elevation: 8 },
  fabTexto: { color: '#FFD700', fontSize: 28, fontWeight: '300', lineHeight: 32 },
});
