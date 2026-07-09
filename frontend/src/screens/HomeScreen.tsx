import React, { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  ActivityIndicator, RefreshControl, StatusBar, Alert, TextInput, ScrollView
} from 'react-native';
import { api } from '../api/config';
import AsyncStorage from '@react-native-async-storage/async-storage';

// US03, US09, US11
export default function HomeScreen({ navigation }: any) {
  const [sesiones, setSesiones]           = useState<any[]>([]);
  const [loading, setLoading]             = useState(true);
  const [refreshing, setRefreshing]       = useState(false);
  const [searchQuery, setSearchQuery]     = useState('');
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
    }
  }, []);

  useEffect(() => {
    cargarSesiones();
    cargarContadorNotificaciones();
  }, [cargarSesiones, cargarContadorNotificaciones]);

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
    if (query.trim() === '') { cargarSesiones(); return; }
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
    if (!cat) { cargarSesiones(); return; }
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
    if (!mod) { cargarSesiones(); return; }
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

  const estadoColor: Record<string, string> = {
    ACTIVA: '#22C55E',
    PENDIENTE: '#F59E0B',
    CANCELADA: '#EF4444',
    FINALIZADA: '#8898AA',
  };

  const renderSesion = ({ item }: any) => (
    <TouchableOpacity
      style={styles.card}
      onPress={() => navigation.navigate('DetalleSesion', { sesionId: item.sesionId })}
      activeOpacity={0.85}
    >
      {/* Franja de acento lateral */}
      <View style={[styles.cardAccent, { backgroundColor: item.tipo === 'PUBLICA' ? '#F97316' : '#6366F1' }]} />
      <View style={styles.cardBody}>
        <View style={styles.cardTopRow}>
          <View style={[styles.tipoBadge, { backgroundColor: item.tipo === 'PUBLICA' ? '#FFF0E6' : '#EEEEFF' }]}>
            <Text style={[styles.tipoBadgeTexto, { color: item.tipo === 'PUBLICA' ? '#F97316' : '#6366F1' }]}>
              {item.tipo === 'PUBLICA' ? 'Pública' : 'Privada'}
            </Text>
          </View>
          {item.modalidad && (
            <View style={styles.modalidadBadge}>
              <Text style={styles.modalidadTexto}>{item.modalidad}</Text>
            </View>
          )}
        </View>
        <Text style={styles.cardTitulo} numberOfLines={2}>{item.titulo}</Text>
        {item.categoria && <Text style={styles.cardCategoria}>{item.categoria}</Text>}
        <View style={styles.cardMeta}>
          <Text style={styles.cardMetaTexto}>
            {item.instructorNombre || 'Instructor'}
          </Text>
          <Text style={styles.cardMetaSep}>·</Text>
          <Text style={styles.cardMetaTexto}>
            {item.fechaSesion ? new Date(item.fechaSesion).toLocaleDateString('es-PE', { day: '2-digit', month: 'short' }) : 'Por confirmar'}
          </Text>
        </View>
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0F1C36" />

      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.headerMarca}>SkillSharing</Text>
          <Text style={styles.headerSub}>Descubre qué aprender hoy</Text>
        </View>
        <View style={styles.headerAcc}>
          <TouchableOpacity style={styles.notifBtn} onPress={() => navigation.navigate('Notificaciones')}>
            <Text style={styles.notifBtnTexto}>!</Text>
            {unreadNotifications > 0 && (
              <View style={styles.notifBadge}>
                <Text style={styles.notifBadgeTexto}>{unreadNotifications}</Text>
              </View>
            )}
          </TouchableOpacity>
          <TouchableOpacity onPress={handleLogout}>
            <Text style={styles.salirTexto}>Salir</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Buscador */}
      <View style={styles.searchWrap}>
        <TextInput
          style={styles.searchInput}
          placeholder="Buscar sesión..."
          value={searchQuery}
          onChangeText={buscarSesiones}
          placeholderTextColor="#8898AA"
        />
      </View>

      {/* Filtros modalidad */}
      <View style={styles.filtrosRow}>
        {[null, 'VIRTUAL', 'PRESENCIAL'].map((mod) => (
          <TouchableOpacity
            key={String(mod)}
            style={[styles.filterChip, selectedModalidad === mod && styles.filterChipActive]}
            onPress={() => filtrarModalidad(mod)}
          >
            <Text style={[styles.filterChipText, selectedModalidad === mod && styles.filterChipTextActive]}>
              {mod === null ? 'Todas' : mod === 'VIRTUAL' ? 'Virtual' : 'Presencial'}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Filtros categorías */}
      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.categoriaScroll}>
        {[null, ...categorias].map((cat) => (
          <TouchableOpacity
            key={String(cat)}
            style={[styles.catChip, selectedCategory === cat && styles.catChipActive]}
            onPress={() => filtrarCategoria(cat)}
          >
            <Text style={[styles.catChipTexto, selectedCategory === cat && styles.catChipTextoActive]}>
              {cat === null ? 'Todas' : cat}
            </Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      <Text style={styles.seccionLabel}>
        {sesiones.length > 0 ? `${sesiones.length} sesiones disponibles` : 'Sesiones disponibles'}
      </Text>

      {loading ? (
        <ActivityIndicator size="large" color="#F97316" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={sesiones}
          keyExtractor={(item) => String(item.sesionId)}
          renderItem={renderSesion}
          contentContainerStyle={styles.lista}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); cargarSesiones(); cargarContadorNotificaciones(); }} colors={['#F97316']} />}
          ListEmptyComponent={<Text style={styles.vacio}>Todavía no hay sesiones publicadas.</Text>}
        />
      )}

      {/* Bottom Nav */}
      <View style={styles.bottomNav}>
        <TouchableOpacity style={styles.navItem} onPress={() => {}}>
          <View style={styles.navDot} />
          <Text style={[styles.navTexto, { color: '#F97316' }]}>Inicio</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.navItem} onPress={() => navigation.navigate('MisSesiones')}>
          <Text style={styles.navTexto}>Mis sesiones</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.fabNav} onPress={() => navigation.navigate('CrearSesion')}>
          <Text style={styles.fabNavTexto}>+</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.navItem} onPress={() => navigation.navigate('BuscarInstructores')}>
          <Text style={styles.navTexto}>Instructores</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.navItem} onPress={() => navigation.navigate('Perfil')}>
          <Text style={styles.navTexto}>Perfil</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F0F4F8' },
  header: {
    backgroundColor: '#0F1C36',
    paddingTop: 52,
    paddingBottom: 20,
    paddingHorizontal: 20,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
  },
  headerMarca: { color: '#F97316', fontSize: 22, fontWeight: '900', letterSpacing: 0.5 },
  headerSub: { color: '#8898AA', fontSize: 12, marginTop: 2 },
  headerAcc: { flexDirection: 'row', alignItems: 'center', gap: 14 },
  notifBtn: {
    width: 36, height: 36, borderRadius: 12,
    backgroundColor: '#1A2E50',
    justifyContent: 'center', alignItems: 'center',
  },
  notifBtnTexto: { color: '#FFFFFF', fontSize: 18, fontWeight: '700', lineHeight: 22 },
  notifBadge: {
    position: 'absolute', top: -4, right: -4,
    backgroundColor: '#F97316', borderRadius: 10,
    width: 18, height: 18, justifyContent: 'center', alignItems: 'center',
  },
  notifBadgeTexto: { color: '#fff', fontSize: 10, fontWeight: '800' },
  salirTexto: { color: '#8898AA', fontSize: 13, fontWeight: '600' },
  searchWrap: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: '#0F1C36',
  },
  searchInput: {
    backgroundColor: '#1A2E50',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    color: '#FFFFFF',
    fontSize: 14,
  },
  filtrosRow: {
    flexDirection: 'row',
    gap: 8,
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E8EDF2',
  },
  filterChip: {
    paddingHorizontal: 14, paddingVertical: 7,
    borderRadius: 20, backgroundColor: '#F0F4F8',
    borderWidth: 1.5, borderColor: '#E2E8F0',
  },
  filterChipActive: { backgroundColor: '#0F1C36', borderColor: '#0F1C36' },
  filterChipText: { fontSize: 12, color: '#4A5568', fontWeight: '700' },
  filterChipTextActive: { color: '#FFFFFF' },
  categoriaScroll: { paddingHorizontal: 16, paddingVertical: 10, gap: 8 },
  catChip: {
    paddingHorizontal: 14, paddingVertical: 7,
    borderRadius: 20, backgroundColor: '#FFFFFF',
    borderWidth: 1.5, borderColor: '#E2E8F0',
  },
  catChipActive: { backgroundColor: '#F97316', borderColor: '#F97316' },
  catChipTexto: { fontSize: 12, color: '#4A5568', fontWeight: '600' },
  catChipTextoActive: { color: '#FFFFFF', fontWeight: '700' },
  seccionLabel: {
    fontSize: 13, fontWeight: '700', color: '#8898AA',
    marginHorizontal: 16, marginTop: 4, marginBottom: 2,
  },
  lista: { paddingHorizontal: 16, paddingBottom: 100, paddingTop: 8 },
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    marginBottom: 12,
    flexDirection: 'row',
    overflow: 'hidden',
    shadowColor: '#0F1C36',
    shadowOffset: { width: 0, height: 3 },
    shadowOpacity: 0.08,
    shadowRadius: 8,
    elevation: 3,
  },
  cardAccent: { width: 5 },
  cardBody: { flex: 1, padding: 16 },
  cardTopRow: { flexDirection: 'row', gap: 8, marginBottom: 8 },
  tipoBadge: { paddingHorizontal: 10, paddingVertical: 3, borderRadius: 20 },
  tipoBadgeTexto: { fontSize: 11, fontWeight: '800' },
  modalidadBadge: {
    paddingHorizontal: 10, paddingVertical: 3, borderRadius: 20,
    backgroundColor: '#F0F4F8',
  },
  modalidadTexto: { fontSize: 11, color: '#4A5568', fontWeight: '700' },
  cardTitulo: { fontSize: 15, fontWeight: '800', color: '#0F1C36', lineHeight: 21, marginBottom: 4 },
  cardCategoria: { fontSize: 12, color: '#F97316', fontWeight: '700', marginBottom: 8 },
  cardMeta: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  cardMetaTexto: { fontSize: 12, color: '#8898AA', fontWeight: '600' },
  cardMetaSep: { color: '#CBD5E0', fontSize: 12 },
  vacio: { textAlign: 'center', color: '#8898AA', marginTop: 60, fontSize: 15 },
  bottomNav: {
    position: 'absolute', bottom: 0, left: 0, right: 0,
    height: 72,
    backgroundColor: '#FFFFFF',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    borderTopWidth: 1,
    borderTopColor: '#E8EDF2',
    paddingBottom: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.06,
    shadowRadius: 8,
    elevation: 10,
  },
  navItem: { alignItems: 'center', flex: 1 },
  navDot: { width: 4, height: 4, borderRadius: 2, backgroundColor: '#F97316', marginBottom: 2 },
  navTexto: { fontSize: 11, color: '#8898AA', fontWeight: '700' },
  fabNav: {
    width: 54, height: 54, borderRadius: 27,
    backgroundColor: '#F97316',
    justifyContent: 'center', alignItems: 'center',
    marginBottom: 10,
    shadowColor: '#F97316',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.4,
    shadowRadius: 10,
    elevation: 8,
  },
  fabNavTexto: { color: '#FFFFFF', fontSize: 28, fontWeight: '300', lineHeight: 32 },
});
